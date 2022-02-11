# Coalition Structure Generation (CSG) Problem:
This repository contains the code to reproduce the results presented in the paper BILP-Q: Quantum Coalition Structure Generation, submitted in the 
[19th ACM International Conference on Computing Frontiers (CF'22)](https://www.computingfrontiers.org/2022/index.html).
## BILP-Q: Quantum Coalition Structure Generation

We propose BILP-Q, the first general quantum  approach for solving the Coalition Structure Generation problem
(CSGP). In particular, we reformulate the CSGP in terms of a Quadratic Binary Combinatorial Optimization
(QUBO) problem so that it is possible to leverage existing quantum algorithms to obtain the best coalition structure.
Thus we perform a comparative analysis in terms of time complexity between the proposed quantum algorithm and the most popular
classical baselines. Furthermore, we consider standard benchmark distributions for coalition values to test the BILP-Q on small-scale
problem instances using the [IBM Qiskit](https://qiskit.org/) environment. Finally, since QUBO problems can be solved operating with quantum annealing, we run
BILP-Q on medium-size problems using a real quantum annealer device ([Dwave](https://www.dwavesys.com/)).

## Usage
The code is organized in different scripts in this repo to run the experiments. These scripts uses three main approaches in fetching the solution of the input CSG problem instance. A breif description of them are as follows:
- *Gate Based*: uses the [**QAOA**](https://qiskit.org/textbook/ch-applications/qaoa.html) quantum algortihm to solve the QUBO formulation of the corresponding CSG problem
- *Quantum Annealing*: uses the [**D-Wave**](https://www.dwavesys.com/) quantum annealer to solve the input QUBO problem.
- *Exact Solver*: uses classical computing to solve the BILP formulation, to  verify the results from the quantum approaches.

The script *data_generator.py* is used to generate problem instances (characterisctic function) using different distributions for experimental analysis.

The script *Utils_CSG.py* contains the helper functions in structuring the outputs and generating the reports. For example, *convert_to_BILP()* function formulates the BILP problem for a given CSG problem instance, *get_QUBO_coeffs()* function converts the BILP problem instance into linear and quadratic terms required for QUBO formulation, *decode()* function converts the solution binary string into a coalition sructure(a list of coalitions).

The script *Utils_Solvers.py* contains the functions to use the APIs of dependencies like *qiskit*, *dimod*, *d-wave* for solving the input problem instances using the above three strategies.

The script  *BILP-Q_benchmark.py* contains the configurations to set-up for starting the experiments.

## Installation
In order to run the code and reproduce the results of the paper, it is recommended to re-create the same testing environment following the procedure below.

*Note: tested on linux OS only; it assumes Anaconda is installed*
 - First, clone the repository:
```sh
git clone https://github.com/supreethmv/CSGP.git
```
 - Environment Setup:
It is a good practice to create a virtual environment to run any project.
Start a terminal in the root directory after cloning the repository:
```sh
#enter the repository
cd .\CSGP

#create a virtual environment 
virtualenv <name of the environnment>
#or
venv <name of the environnment>
```
```
#Activate the virtual environment
.\<name of the environnment>\Scripts\activate

#Install dependencies
pip install requirements.txt -r
```
Set the configurations for running the experiments in the file *BILP-Q_benchmark.py* and run the prooject:
```
python .\BILP-Q_benchmark.py
```
A successful execution of the script will generate outputs according the parameters set.

The following are parameters to be set in the file *BILP-Q_benchmark.py* before running the experiments are:
 - distributions: list of function names of distributions in *Utils_CSG.py*
 - n_agents: list of integers mentioning the number of agents considered for experiments.
 - root_folder: root folder for the outputs.
 - penalty: hyper parameter to reduce contraint problem to unconstraint problem, large value is recommended as n_agents increase.
 - dwave_runs: number of runs for each instance on the dwave system.
 - create_file: Boolean value, if True, a new directory is created for each Distribution.
 - seed: seed value for numpy to generate random numbers.
 - QAOA: Boolean value to specify to coonsideration of QAOA for running the experiments.
 - dwave: Boolean value to specify to coonsideration of dwave system for running the experiments.
 - exact: Boolean value to specify to coonsideration of dwave system exactly for running the experiments.
 - classical_BILP: Boolean value to specify to coonsideration of classically solving equivalent BILP problem for running the experiments.
 - folder: subfolder inside root_folder to save the results

### To generate the results in paper:
The experiments are run for different penalty parameters. As the size of the problem(number of agents) increases, we have to increase the penalty parameters. Penalty parameter plays a vital role as the problem is reduced from a constrained (BILP) to unconstrained optimization (QUBO), the constraints are added as new terms with a penalty parameter coefficient to the original optimization function. So, it is necessary to assign a higher value with the number of agents to consider only the solutions that comply with the constraints of the original problem.

The final report is generated by considering the best results in terms of two criteria, namely the lowest function value(optimization problem is more specifically a minimization problem) and best rank in terms of the probabilities generated for all possible solution binary strings.

There are two notebooks in this repository to help collating the experimental results.
 - *Collect_results.ipynb* is used to take the root folder path of the generated output and creates a table of function value, probability, rank of the probability and time taken along with the best penalty parameter for each distribution and the number of agents and a graph to visualize the time taken by the Quantum Annealer to execute(**Figure 2** in the paper). Also, further in this notebook, the results from the QAOA results are fetched and creates a final table of results with best parameter choices from the conducted experiments (same as the **Table 1** in the paper).
 - *Complexity.ipynb* considers the theoretical time complexity of various classical solvers like Integer Partition(IP) solver for CSG problem, Bi-directional Search Technique for Optimal Coalition Structure Generation with Minimal Overlapping(BOSS), Improved Dynamic Programming(IDP), BILP solver in comparision with the proposed approach of BILP-Q and the notebook generates a visualization of the order of growth as a graph(**Figure X** in the paper). Since the time taken by BILP-Q mainly depends on the number of layers(*p*) in the QAOA circuit, we have considered the order of growth of complexity for BILP-Q for a range of [1,50] for *p* to  plot the graph.

##Issues
For any issues or questions related to the code, open a new git issue or send a mail to XXXXXXXXX