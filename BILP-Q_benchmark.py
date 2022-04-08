from Utils_CSG import *
from Utils_Solvers import *


def running_dwave(linear, quadratic, exact_solution, colnames,
                  params={'distr':'', 'n':0}, n_runs=1000):
    """
    Solve the experimental input instance using the dwave device
    :params
    linear: dictionary of linear coefficient terms in the QUBO formulation of the CSG problem.
    quadratic: dictionary of quadratic coefficient terms in the QUBO formulation of the CSG problem.
    exact_solution: This is the exact solution of the input problem instance for verifying the output_ from dwave system.
    colnames: list of column headers for generating a report using a standard schema.
    
    :return
    row: pandas Series object consisting of outputs and input distribution name and agents to generate the final report.
    sample_set_dwave: input parameters used for solving the problem instance using dwave system.
    """
    print(f'running dwave  -  {params["distr"]}  -  {params["n"]}')

    sample_set_dwave = dwave_solver(linear, quadratic, runs=n_runs)
    solution, fval, prob, rank, time_run = results_from_dwave(sample_set_dwave)
    flag = exact_solution == solution
    row = pd.Series([params['distr'], params['n'], solution, None, fval, prob,
                     rank, time_run, 'dwave', flag, None, None], index=colnames)
    return row, sample_set_dwave


def running_dwave_exact(linear, quadratic, exact_solution, colnames,
                        params={'distr':'', 'n':0}):
    """
    Solve the experimental data input using the dwave device exactly
    :params
    linear: dictionary of linear coefficient terms in the QUBO formulation of the CSG problem.
    quadratic: dictionary of quadratic coefficient terms in the QUBO formulation of the CSG problem.
    exact_solution: This is the exact solution of the input problem instance for verifying the output_ from dwave system.
    colnames: list of column headers for generating a report using a standard schema.
    
    :return
    row: pandas Series object consisting of outputs and input distribution name and agents to generate the final report.
    sample_set_dwave: input parameters used for solving the problem instance using dwave system.
    """
    print(f'exact dwave  -  {params["distr"]}  -  {params["n"]}')

    start = time.time()
    sample_set_exact = exact_solver(linear, quadratic)
    end = time.time()
    time_run = end - start

    solution, fval, prob, rank, _ = results_from_dwave(sample_set_exact, exact=True)
    flag = exact_solution == solution
    row = pd.Series([params['distr'], params['n'], solution, None, fval, prob,
                     rank, time_run, 'exact dwave', flag, None, None], index=colnames)

    return row, sample_set_exact


def running_QAOA(linear, quadratic, exact_solution, colnames,
                 params={'distr':'', 'n':0}, n_init=20, p_list=np.arange(1,20)):
    """
    Solve the experimental data input using the QAOA
    :params
    linear: dictionary of linear coefficient terms in the QUBO formulation of the CSG problem.
    quadratic: dictionary of quadratic coefficient terms in the QUBO formulation of the CSG problem.
    exact_solution: This is the exact solution of the input problem instance for verifying the output_ from dwave system.
    colnames: list of column headers for generating a report using a standard schema.
    params: input parameterization for qiskit's QAOA
    
    :return
    row: pandas Series object consisting of outputs and input distribution name and agents to generate the final report.
    sample_set_dwave: input parameters used for solving the problem instance using dwave system.
    """
    print(f'running qaoa  -  {params["distr"]}  -  {params["n"]}')

    qaoa_result, p, init, _ = QAOA_optimization(linear, quadratic, n_init=n_init, p_list=p_list)
    solution, fval, prob, rank, time_run = results_from_QAOA(qaoa_result)
    flag = sum(exact_solution == solution)==len(solution)
    print(exact_solution, '\n', solution)
    row = pd.Series([params['distr'], params['n'], solution, p, fval, prob,
                     rank+1, time_run, 'QAOA', flag, None, None], index = colnames)
    return row, qaoa_result, p, init



def run_all(distributions, n_agents, root_folder, penalty=None, dwave_runs = 1000,
            create_file = True, seed=12345,
            QAOA=True, dwave=True, exact=True, classical_BILP=True, folder='__'):
    """
    Solve the experimental data input using the dwave device
    :params
    distributions: list of function names of distributions in Utils_CSG.py .
    n_agents: list of integers mentioning the number of agents considered for experiments.
    root_folder: root folder for the outputs.
    penalty: hyper parameter to reduce contraint problem to unconstraint problem, large value is recommended as n_agents increase.
    dwave_runs: number of runs for each instance on the dwave system.
    create_file: Boolean value, if True, a new directory is created for each Distribution.
    seed: seed value for numpy to generate random numbers.
    QAOA: Boolean value to specify to coonsideration of QAOA for running the experiments.
    dwave: Boolean value to specify to coonsideration of dwave system for running the experiments.
    exact: Boolean value to specify to coonsideration of dwave system exactly for running the experiments.
    classical_BILP: Boolean value to specify to coonsideration of classically solving equivalent BILP problem for running the experiments.
    folder: subfolder inside root_folder to save the results
    
    :return
    None
    """

    root_folder = os.path.join(root_folder, f'{seed}', folder)
    create_dir(root_folder)

    colnames = ["distribution", "n_agents", "solution", "p", "fval", "prob",
                "rank", "time", "device", "flag", "time_bilp", "penalty"]

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
                exact_solution = (2 ** n) * [0.]
                bilp_time = None


            ## Dwave
            if dwave:
                row, sample_set = running_dwave(linear, quadratic, exact_solution, colnames,
                                                n_runs=dwave_runs, params={'distr':distr, 'n':n})
                row['time_bilp'] = bilp_time
                row['penalty'] = penalty
                row = pd.DataFrame(row).transpose()


                row.to_csv(os.path.join(path, dwave_file), mode='a', index=False)
                row.to_csv(path_distr, mode='a', index=False, header=False)
                row.to_csv(path_all, mode='a', index=False, header=False)

                sample_set.to_pandas_dataframe().to_csv(os.path.join(path, 'metadata', 'dwave_distr.csv'), index=False)

            ## Exact Dwave
            if exact:
                row, sample_set = running_dwave_exact(linear, quadratic,
                                                      exact_solution, colnames, params={'distr':distr, 'n':n})
                row['time_bilp'] = bilp_time
                row['penalty'] = penalty
                row = pd.DataFrame(row).transpose()

                row.to_csv(os.path.join(path, exact_dwave), mode='a', index=False)
                row.to_csv(path_distr, mode='a', index=False, header=False)
                row.to_csv(path_all, mode='a', index=False, header=False)

                sample_set.to_pandas_dataframe().to_csv(os.path.join(path, 'metadata', 'exact_dwave_distr.csv'), index=False)


            # QAOA
            if QAOA:
                row, qaoa_result, p, init = running_QAOA(linear, quadratic,
                                                         exact_solution, colnames, params={'distr':distr, 'n':n})
                row['time_bilp'] = bilp_time
                row['penalty'] = penalty
                row = pd.DataFrame(row).transpose()

                row.to_csv(os.path.join(path, qaoa_file), mode='a', index=False)
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
    # shutil.rmtree(r'output_')

    seed = 12
    root = 'output'
    create_dir(root)


    # Running the experiments for all distributions, with 2 and 3 agents
    distributions = [Agent_based_uniform, Agent_based_normal, Modified_uniform_distribution,
                     Normal_distribution, Weibull_distribution, Weighted_random_with_chisquare,
                     F_distribution, Laplace_or_double_exponential, Rayleigh_distribution,
                     SVA_BETA_distribution]
    n_agents = [2,3]
    penalty = 100
    run_all(distributions, n_agents, root, penalty=penalty,
            create_file=True, seed=seed, QAOA=True, dwave=True, exact=True,
            classical_BILP=True, folder='QAOA_QA_23')

    penalty = 1000
    run_all(distributions, n_agents, root, penalty=penalty,
            create_file=True, seed=seed, QAOA=True, dwave=True, exact=True,
            classical_BILP=True, folder='QAOA_QA_23')

    # ----------------------------------------------------------------------------- #

    # Running the Quantum Annealing (Dwave) solution for all distributions, from 2 to 7 agents
    distributions = [Agent_based_uniform, Agent_based_normal, Modified_uniform_distribution,
                     Normal_distribution, Weibull_distribution, Weighted_random_with_chisquare,
                     F_distribution, Laplace_or_double_exponential, Rayleigh_distribution,
                     SVA_BETA_distribution]

    for i in range(3,8):
        n_agents = [4, 5, 6, 7]

        penalty = 10**i

        run_all(distributions, n_agents, root, penalty=penalty, dwave_runs=1000,
                create_file=True, seed=seed, QAOA=False, dwave=True, exact=False, classical_BILP=False,
                folder=f'QA_hyper_params_47_{i}')
