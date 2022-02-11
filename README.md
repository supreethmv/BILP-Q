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
 - *distributions*: list of function names of distributions in *Utils_CSG.py*
 - *n_agents*: list of integers mentioning the number of agents considered for experiments.
 - *root_folder*: root folder for the outputs.
 - *penalty*: hyper parameter to reduce contraint problem to unconstraint problem, large value is recommended as n_agents increases.
 - *dwave_runs*: number of runs for each instance on the dwave system.
 - *create_file*: Boolean value, if True, a new directory is created for each distribution.
 - *seed*: seed value for numpy to generate random numbers.
 - *QAOA*: Boolean value to specify  whether using QAOA for the running experiments.
 - *dwave*: Boolean value to specify  whether using DWave for the running experiments.
 - *exact*: Boolean value to specify  whether using Classical BILP solver for the running experiments.
 - *classical_BILP*: Boolean value to specify to coonsideration of classically solving equivalent BILP problem for running the experiments.
 - *folder*: subfolder inside root_folder to save the results

### Results:
The results reported in the paper are contained in the output folder.

The penalty parameter <img src="https://render.githubusercontent.com/render/math?math=\lambda"> plays a crucial role as the problem is reduced from a constrained (BILP) to unconstrained optimization (QUBO) since the constraints are added as new terms with a penalty parameter coefficient to the original optimization function. Thus, as the size of the problem (in terms of the number of agents) increases, the penalty parameter is also expected to increase. For this reason, the experiments are run for different values of the penalty parameter.

The final report is generated by considering the best results in terms of two criteria: the best value for the optimization function and the best rank in terms of the probabilities generated for all possible binary strings.

There are two notebooks in this repository to help collect the experimental results.
 - *Collect_results.ipynb* is used to take the root folder path of the generated output and create a table of function value, probability, the rank of the probability, and time taken along with the best penalty parameter for each distribution and number of agents. 
As a final result, a graph to visualize results of BILP-Q using the DWave quantum annealing is generated (**Figure 2** in the paper). 
Also, further in this notebook, the results from the QAOA are collected a final table is reported with the best $p$ parameter of QAOA chosen from the conducted experiments (same as the **Table 1** in the paper).
 - *Complexity.ipynb* considers the theoretical time complexity of various classical solvers like Integer Partition(IP), 
Bi-directional Search Technique for Optimal Coalition Structure Generation with Minimal Overlapping(BOSS), 
Improved Dynamic Programming(IDP), BILP solver in comparison with the proposed BILP-Q. 
The code generates a graph where the <img src="https://render.githubusercontent.com/render/math?math=log_2"> of the cost
complexity for various algorithms is plotted as a function of the number of agents (**Figure 1** in the paper). 

##Issues
For any issues or questions related to the code, open a new git issue or send a mail to
[antonio.macaluso@dfki.de](antonio.macaluso@dfki.de) or [s8sumyso@stud.uni-saarland.de](s8sumyso@stud.uni-saarland.de).
