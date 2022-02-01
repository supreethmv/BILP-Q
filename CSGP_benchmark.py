from Utils import *
from Utils_CSG import *
from Utils_Solvers import *


def running_dwave(linear, quadratic, exact_solution, colnames,
                  params={'distr':'', 'n':0}):
    print(f'running dwave  -  {params["distr"]}  -  {params["n"]}')

    sample_set_dwave = dwave_solver(linear, quadratic)
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
                 params={'distr':'', 'n':0}, n_init=5, p_list=np.arange(10,20)):

    print(f'running qaoa  -  {params["distr"]}  -  {params["n"]}')

    qaoa_result, p, init, _ = QAOA_optimization(linear, quadratic, n_init=n_init, p_list=p_list)
    solution, fval, prob, rank, time_run = results_from_QAOA(qaoa_result)
    flag = sum(exact_solution == solution)==len(solution)
    print(exact_solution, '\n', solution)
    row = pd.Series([params['distr'], params['n'], solution, p, fval, prob,
                     rank, time_run, 'QAOA', flag, None], index = colnames)
    return row, qaoa_result, p, init



def run_all(distributions, n_agents, root_folder):

    create_dir(root_folder)

    colnames = ["distribution", "n_agents", "solution", "p", "fval",
                "prob", "rank", "time", "device", "flag", "time_bilp"]

    path_all = os.path.join(root_folder, 'all_results.csv')
    qaoa_file, dwave_file, exact_dwave = 'qaoa.csv', 'dwave.csv', 'exact_dwave.csv'

    # Create file
    # df_complete = pd.DataFrame(columns=colnames)
    # df_complete.to_csv(path_all, index=False)



    for distribution in distributions:

        distr = distribution.__name__

        # Create file
        path_distr = os.path.join(root_folder, distr)
        create_dir(path_distr)

        # df_distribution = pd.DataFrame(columns=colnames)
        # df_distribution.to_csv(os.path.join(path_distr, 'distr_results.csv'), index=False)

        for n in n_agents:
            path = os.path.join(root_folder, distr, 'n_' + str(n))
            create_dir(path, log=True)

            np.random.seed(seed=1234)
            coalition_values, linear, quadratic = get_linear_quads(distribution, n, 10**n)


            start = time.time()
            exact_solution = list(map(np.float, list(solve_BILP_classical(coalition_values))))
            end = time.time()
            bilp_time = (end - start)*1000


            ## Dwave
            row, _ = running_dwave(linear, quadratic, exact_solution, colnames, params={'distr':distr, 'n':n})
            row['time_bilp'] = bilp_time
            row = pd.DataFrame(row).transpose()

            row.to_csv(os.path.join(path, dwave_file), mode='a', index=False, header=False)
            row.to_csv(os.path.join(path_distr, 'distr_results.csv'), mode='a', index=False, header=False)
            row.to_csv(path_all, mode='a', index=False, header=False)

            ## Exact Dwave
            row, _ = running_dwave_exact(linear, quadratic, exact_solution, colnames, params={'distr':distr, 'n':n})
            row['time_bilp'] = bilp_time
            row = pd.DataFrame(row).transpose()

            row.to_csv(os.path.join(path, exact_dwave), mode='a', index=False, header=False)
            row.to_csv(os.path.join(path_distr, 'distr_results.csv'), mode='a', index=False, header=False)
            row.to_csv(path_all, mode='a', index=False, header=False)


            # QAOA
            row, qaoa_result, p, init = running_QAOA(linear, quadratic, exact_solution, colnames, params={'distr':distr, 'n':n})
            row['time_bilp'] = bilp_time
            row = pd.DataFrame(row).transpose()

            row.to_csv(os.path.join(path, qaoa_file), mode='a', index=False, header=False)
            row.to_csv(os.path.join(path_distr, 'distr_results.csv'), mode='a', index=False, header=False)
            row.to_csv(path_all, mode='a', index=False, header=False)



            # ----------------------------------------------------------------------- #

            ## Save within folder
            df, data_solution = ranking_results_QAOA(qaoa_result, exact_solution)
            df.to_csv(os.path.join(path, 'qaoa_distr.csv'), index=False)

            with open(os.path.join(path, 'metadata.txt'), "w") as output:
                output.write(f'coalition_values: {coalition_values}    \n')
                output.write(f'linear: {linear}    \n')
                output.write(f'quadratic: {quadratic}    \n')
                output.write(f'p:    {p}    \n')
                output.write(f'init: {init} \n')
                output.write(f'solution: {data_solution} \n')


if __name__=="__main__":

    # import shutil
    # shutil.rmtree(r'output')
    root_folder = 'output_4'

    distributions = [Agent_based_uniform, Agent_based_normal, Modified_uniform_distribution,
                     Normal_distribution, SVA_BETA_distribution, Weibull_distribution, Rayleigh_distribution,
                     Weighted_random_with_chisquare, F_distribution,Laplace_or_double_exponential]
    n_agents = [4]

    run_all(distributions, n_agents, root_folder)
