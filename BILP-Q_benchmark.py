from Utils import *
from Utils_CSG import *
from Utils_Solvers import *


def running_dwave(linear, quadratic, exact_solution, colnames,
                  params={'distr':'', 'n':0}, n_runs=1000):
    print(f'running dwave  -  {params["distr"]}  -  {params["n"]}')

    sample_set_dwave = dwave_solver(linear, quadratic, runs=n_runs)
    solution, fval, prob, rank, time_run = results_from_dwave(sample_set_dwave)
    flag = exact_solution == solution
    row = pd.Series([params['distr'], params['n'], solution, None, fval, prob,
                     rank, time_run, 'dwave', flag, None], index=colnames)
    return row, sample_set_dwave


def running_dwave_exact(linear, quadratic, exact_solution, colnames,
                        params={'distr':'', 'n':0}):
    print(f'exact dwave  -  {params["distr"]}  -  {params["n"]}')

    start = time.time()
    sample_set_exact = exact_solver(linear, quadratic)
    end = time.time()
    time_run = end - start

    solution, fval, prob, rank, _ = results_from_dwave(sample_set_exact, exact=True)
    flag = exact_solution == solution
    row = pd.Series([params['distr'], params['n'], solution, None, fval, prob,
                     rank, time_run, 'exact dwave', flag, None], index=colnames)

    return row, sample_set_exact


def running_QAOA(linear, quadratic, exact_solution, colnames,
                 params={'distr':'', 'n':0}, n_init=20, p_list=np.arange(1,20)):

    print(f'running qaoa  -  {params["distr"]}  -  {params["n"]}')

    qaoa_result, p, init, _ = QAOA_optimization(linear, quadratic, n_init=n_init, p_list=p_list)
    solution, fval, prob, rank, time_run = results_from_QAOA(qaoa_result)
    flag = sum(exact_solution == solution)==len(solution)
    print(exact_solution, '\n', solution)
    row = pd.Series([params['distr'], params['n'], solution, p, fval, prob,
                     rank+1, time_run, 'QAOA', flag, None], index = colnames)
    return row, qaoa_result, p, init



def run_all(distributions, n_agents, root_folder, penalty=None, dwave_runs = 1000,
            create_file = True, seed=12345,
            QAOA=True, dwave=True, exact=True, classical_BILP=True, folder='__'):

    root_folder=os.path.join(root_folder, f'{seed}', folder)
    create_dir(root_folder)

    colnames = ["distribution", "n_agents", "solution", "p", "fval",
                "prob", "rank", "time", "device", "flag", "time_bilp"]

    path_all = os.path.join(root_folder, 'all_results.csv')
    qaoa_file, dwave_file, exact_dwave = 'qaoa.csv', 'dwave.csv', 'exact_dwave.csv'

    # Create file
    if create_file:
        df_complete = pd.DataFrame(columns=colnames)
        df_complete.to_csv(path_all, index=False)



    for distribution in distributions:

        distr = distribution.__name__

        # Create file
        create_dir(os.path.join(root_folder, distr))
        path_distr = os.path.join(root_folder, distr, 'distr_results.csv')

        if create_file:
            df_distribution = pd.DataFrame(columns=colnames)
            df_distribution.to_csv(path_distr, index=False)

        for n in n_agents:

            if penalty is None:
                penalty=10**(n+1)

            path = os.path.join(root_folder, distr, 'n_' + str(n))

            create_dir(path, log=True)
            create_dir(os.path.join(path, 'metadata'))

            np.random.seed(seed=seed)
            coalition_values, linear, quadratic = get_linear_quads(distribution, n, penalty)


            if classical_BILP:
                start = time.time()
                exact_solution = list(map(np.float, list(solve_BILP_classical(coalition_values))))
                end = time.time()
                bilp_time = (end - start)*1000
            else:
                exact_solution = (2 ** n) * [0.]  # list(map(np.float, list(solve_BILP_classical(coalition_values))))
                bilp_time = None


            ## Dwave
            if dwave:
                row, sample_set = running_dwave(linear, quadratic, exact_solution, colnames,
                                                n_runs=dwave_runs, params={'distr':distr, 'n':n})
                row['time_bilp'] = bilp_time
                row = pd.DataFrame(row).transpose()

                row.to_csv(os.path.join(path, dwave_file), mode='a', index=False, header=False)
                row.to_csv(path_distr, mode='a', index=False, header=False)
                row.to_csv(path_all, mode='a', index=False, header=False)

                sample_set.to_pandas_dataframe().to_csv(os.path.join(path, 'metadata', 'dwave_distr.csv'), index=False)

            ## Exact Dwave
            if exact:
                row, sample_set = running_dwave_exact(linear, quadratic,
                                                      exact_solution, colnames, params={'distr':distr, 'n':n})
                row['time_bilp'] = bilp_time
                row = pd.DataFrame(row).transpose()

                row.to_csv(os.path.join(path, exact_dwave), mode='a', index=False, header=False)
                row.to_csv(path_distr, mode='a', index=False, header=False)
                row.to_csv(path_all, mode='a', index=False, header=False)

                sample_set.to_pandas_dataframe().to_csv(os.path.join(path, 'metadata', 'exact_dwave_distr.csv'), index=False)


            # QAOA
            if QAOA:
                row, qaoa_result, p, init = running_QAOA(linear, quadratic,
                                                         exact_solution, colnames, params={'distr':distr, 'n':n})
                row['time_bilp'] = bilp_time
                row = pd.DataFrame(row).transpose()

                row.to_csv(os.path.join(path, qaoa_file), mode='a', index=False, header=False)
                row.to_csv(path_distr, mode='a', index=False, header=False)
                row.to_csv(path_all, mode='a', index=False, header=False)
                # ----------------------------------------------------------------------- #

                ## Save metadata QAOA
                df, data_solution = ranking_results_QAOA(qaoa_result, exact_solution)
                df.to_csv(os.path.join(path, 'metadata', 'qaoa_distr.csv'), index=False)

                with open(os.path.join(path, 'metadata', 'qaoa_metadata.txt'), "w") as output:
                    output.write(f'coalition_values: {coalition_values}    \n')
                    output.write(f'linear: {linear}    \n')
                    output.write(f'quadratic: {quadratic}    \n')
                    output.write(f'p:    {p}    \n')
                    output.write(f'init: {init} \n')
                    output.write(f'solution: {data_solution} \n')


if __name__=="__main__":

    # import shutil
    # shutil.rmtree(r'output')

    distributions = [Agent_based_uniform, Agent_based_normal, Modified_uniform_distribution,
                     Normal_distribution, SVA_BETA_distribution, Weibull_distribution, Rayleigh_distribution,
                     Weighted_random_with_chisquare, F_distribution, Laplace_or_double_exponential]
    seed = 12

    penalty = None
    # with more than 3 agents (n>3) the penalty parameter is updated as 10**n

    root = 'output'
    create_dir(root)


    # Running the Quantum Annealing (Dwave) solution for all distributions, from 2 to 7 agents
    n_agents = [2,3,4,6,7]
    # n_agents = [5]
    run_all(distributions, n_agents, root, penalty, dwave_runs=10000,
            create_file=False, seed=seed, QAOA=False, dwave=True, exact=False, classical_BILP=False, folder='QA_2_7')
    # custom runs for dwave --> 10000


    # # Running the experiments for all distributions, with 2 and 3 agents
    # n_agents = [2,3]
    # run_all(distributions, n_agents, root, penalty,
    #         create_file=True, seed=seed, QAOA=True, dwave=True, exact=True, classical_BILP=True, folder='all_23')

    # Running the experiments for all distributions with QAOA for 4 agents
    # n_agents = [4]
    # root_folder = os.path.join(root, 'QAOA_4')
    # run_all(distributions, n_agents, root_folder, penalty,
    #         create_file=True, seed=seed, QAOA=True, dwave=False, exact=False, classical_BILP=True)
