from Utils import *
from Utils_CSG import *
from Utils_Solvers import *

directory = 'data'

seed = 12

filename = f'data_{seed}.txt'

create_dir(directory, log=False)

distributions = [Agent_based_uniform, Agent_based_normal, Modified_uniform_distribution,
                 Normal_distribution, SVA_BETA_distribution, Weibull_distribution, Rayleigh_distribution,
                 Weighted_random_with_chisquare, F_distribution, Laplace_or_double_exponential]
n_agents = [2, 3, 4, 5, 6, 7]

file = open(os.path.join(directory, filename), 'w')

for distribution in distributions:

    for n in n_agents:

        if n > 3:
            penalty = 10 ** (n+1)
        else:
            penalty = 1000

        file.write(distribution.__name__ + ' ' + str(n))

        np.random.seed(seed=seed)

        coalition_values, linear, quadratic = get_linear_quads(distribution, n, penalty)

        print(distribution.__name__, n)

        for index, coalition in enumerate(coalition_values):
            binkey = bin(index + 1)[2:].zfill(n)
            temp = []
            for agent_index, agent in enumerate(binkey[::-1]):
                if int(agent):
                    temp.append(agent_index + 1)
            file.write(' ' + str(coalition_values[str(sorted(temp))[1:-1].replace(' ', '')]))
        file.write('\n')
file.close()