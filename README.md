# Coalition Structure Generation (CSG) Problem:
This repository contains the code to reproduce the results presented in the paper " XXX ", published in the proceedings of the 19th ACM International Conference on Computing Frontiers (CF'22) and freely available on the ACM Digital Library.
## BILP-Q: Quantum Coalition Structure Generation

The idea underpinning the work is to solve the specific problem of CSG which is part of Coaltion Formation in the domain of Cooperative Games using both Circuit/Gate based and adiabatic quantum computing. To demonstarte this work, we first reformulate the problem to a Binary Integer Linear Programming (BILP) problem. Further, mathematically reduce the BILP formulation to a QUBO problem for solving it using the near-term Quantum Approximate Optimization Algorithm (QAOA). The QUBO formulation can also besolved using the Quantum Annealing using a D-Wave device.

## Usage
The code is organized in different scripts in this repo to run the experiments. These scripts uses three main approaches in fetching the solution of the input CSG problem instance. A breif description of them are as follows:
- *Gate Based*: uses the **QAOA** quantum algortihm to solve the QUBO formulation of the corresponding CSG problem
- *Quantum Annealing*: uses the **D-Wave** computers to solve the input QUBO problem.
- *Exact Solver*: uses classical computing to solve the BILP formulation, to  verify the results from the quantum approaches.

The script *data_generator.py* is used to generate problem instances (characterisctic function) using different distributions for experimental analysis.

The script *Utils.py* is used to XXXXXXXXXXXXXXXXX
The script *Utils_CSG.py* contains  the helper functions in structuring the outputs and generating the reports.
The script *Utils_Solvers.py* contains the functions to use the APIs of dependencies like *qiskit*, *dimod*, *d-wave* for solving the input problem instances using the above three strategies.
The script  *BILP-Q_benchmark.py* contains the configurations to set-up for starting the experiments.

## Installation
In order to run the code and reproduce the results of the paper, it is recommended to re-create the same testing environment following the procedure below.

*Note: tested on linux OS only; it assumes Anaconda is installed*
 - First, clone the repository:
```sh
git clone https://github.com/amacaluso/Quantum-Splines-for-Non-Linear-Approximations.git
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


##Issues
For any issues or questions related to the code, open a new git issue or send a mail to XXXXXXXXX