#!/usr/bin/env python
# coding: utf-8

import warnings
warnings.filterwarnings('ignore')

# Qiskit 
from qiskit import BasicAer
from qiskit.algorithms import QAOA, NumPyMinimumEigensolver
from qiskit_optimization.algorithms import MinimumEigenOptimizer, RecursiveMinimumEigenOptimizer
from qiskit_optimization import QuadraticProgram
from qiskit.utils import QuantumInstance

# Dwaves
import dimod
from dwave.system.samplers import DWaveSampler
from dwave.system.composites import EmbeddingComposite
from dwave.system import LazyFixedEmbeddingComposite

import numpy as np
import pandas as pd
import sympy
import itertools

# import matplotlib.pyplot as plt
from collections import Counter



import math
import numpy as np
import pandas as pd
from sympy import *
import re
import time
import random
import itertools

from qiskit.algorithms.optimizers import COBYLA


#################################### SOLVER

def qaoa_for_qubo(qubo, p=1):                   # QAOA solver for QUBO
  """
  qaoa_for_qubo solves the given QUBO using QAOA

  """
  aqua_globals.random_seed = 123
  initial_point = [np.random.uniform(2*np.pi) for _ in range(2*p)]
  quantum_instance = QuantumInstance(BasicAer.get_backend('qasm_simulator'),       #qasm simulator
                                     seed_simulator=aqua_globals.random_seed,
                                     seed_transpiler=aqua_globals.random_seed)
  qaoa_mes = QAOA(quantum_instance=quantum_instance, initial_point=initial_point,p=p)
  qaoa = MinimumEigenOptimizer(qaoa_mes)    # using QAOA
  result = qaoa.solve(qubo)
  return result



def numpy_for_qubo(qubo, p=None):                      # Classical solver for QUBO
  """
  numpy_for_qubo solves the given QUBO using Numpy library functions
  """
  exact_mes = NumPyMinimumEigensolver()
  exact = MinimumEigenOptimizer(exact_mes)  # using the exact classical numpy minimum eigen solver
  result = exact.solve(qubo)
  return result




def solve_QUBO(linear, quadratic, algo, p=1):
  """
  solve_QUBO is ahigher order function to solve QUBO using the given algo parameter function

  """
  keys = list(linear.keys())
  keys.sort(key=natural_keys)
  qubo = QuadraticProgram()
  for key in keys:
    qubo.binary_var(key)                                                             # initialize the binary variables
  qubo.minimize(linear=linear, quadratic=quadratic)                                  # initialize the QUBO maximization problem instance
  op, offset = qubo.to_ising()
  qp=QuadraticProgram()
  qp.from_ising(op, offset, linear=True)
  result = algo(qubo, p)
  return result

###############################################




def exact_solver(linear, quadratic, offset = 0.0):
    
   
    vartype = dimod.BINARY

    bqm = dimod.BinaryQuadraticModel(linear, quadratic, offset, vartype)
    sampler = dimod.ExactSolver()
    sample_set = sampler.sample(bqm)
    return sample_set



def dwave_solver(linear, quadratic, offset = 0.0, runs=1000):
    
    # linear = linear_dict
    # quadratic = quadratic_dict
    
    vartype = dimod.BINARY

    bqm = dimod.BinaryQuadraticModel(linear, quadratic, 0.0, vartype)
    sampler = EmbeddingComposite(DWaveSampler())
    sample_set = sampler.sample(bqm, num_reads=runs)
    return sample_set



def extract_best_result(df):
    
    row_min = df[df.energy == df.energy.min()]

    cols = []
    for col in df.columns:
        if 'x_' in col:
            cols.append(col)

    x = []

    for col in cols:
        x.append(row_min.iloc[0][col])

    fval = row_min.energy.iloc[0]
    return x, fval

def from_bin_to_var(x, dictionary):
    solution = []
    for i in range(len(x)):
        if x[i] == 1:
            print(list(dictionary.keys())[i])
            solution.append(list(dictionary.keys())[i])
    return solution


def create_QUBO(linear_dict, quadratic_dict):    
    # create a QUBO
    qubo = QuadraticProgram()

    for key in linear_dict.keys():
        qubo.binary_var(key)

    qubo.minimize(linear=linear_dict, quadratic=quadratic_dict)
    return qubo


def from_columns_to_string(df):
    
    cols = []
    for col in df.columns:
        if 'x_' in col:
            cols.append(col)

    df['x'] = 'x'
    for index, row in df.iterrows():
        x = ''
        for col in cols:
            x = x + str(row[col])
        df.loc[index, 'x'] = x
    return df[['x', 'num_occurrences', 'energy']]


def get_ordered_solution(dictionary):
    sortedDict = dict(sorted(dictionary.items(), key=lambda x: x[0].lower()))
    solution = []
    for k, v in sortedDict.items():
        solution.append(v)

    return solution


def results_from_QAOA(result):
    # result = qaoa_result
    solution = get_ordered_solution(result.variables_dict)
    fval = result.fval
    time = result.min_eigen_solver_result.optimizer_time
    

    # get rank
    # flag best
    probabilities = []
    for sample in result.samples:
        probabilities.append(sample.probability)
        if sample.fval == fval:
            prob = sample.probability
    probabilities = sorted(probabilities, reverse=True)
    rank = probabilities.index(prob)

    return solution, fval, prob, rank, time





def results_from_dwave(sample_set, exact=False):
    df = sample_set.to_pandas_dataframe()
    row_min = df[df.energy == df.energy.min()]

    cols = []
    for col in df.columns:
        if 'x_' in col:
            cols.append(col)

    solution = []

    for col in cols:
        solution.append(row_min.iloc[0][col])

    fval = row_min.energy.iloc[0]

    if not exact:
        occ_min_fval = row_min.num_occurrences.to_list()[0]
        occurences = df.num_occurrences.to_list()

        occurences = sorted(occurences, reverse=True)
        
        time = sample_set.info['timing']['qpu_sampling_time']

        rank = occurences.index(occ_min_fval)
        prob = occ_min_fval / sum(df.num_occurrences)
    else:
        rank = 0
        prob = 1
        time = 1

    return solution, fval, prob, rank, time







def QAOA_optimization(linear, quadratic, n_init=10, p_list=np.arange(1,10), info=''):

    
    backend = BasicAer.get_backend('qasm_simulator')
    
    # IBMQ.load_account()
    # provider = IBMQ.get_provider(hub='ibm-q')
    # provider.backends()
    # backend = provider.get_backend('ibmq_qasm_simulator')
    
    optimizer = COBYLA(maxiter=100, rhobeg=2, tol=1.5)  # , disp=True)

    qubo = create_QUBO(linear, quadratic)

    op, offset = qubo.to_ising()
    qp = QuadraticProgram()
    qp.from_ising(op, offset, linear=True)

    ### Initialisation solution
    qaoa_mes = QAOA(optimizer=optimizer, reps=1, quantum_instance=backend, initial_point=[0.,0.])
    qaoa = MinimumEigenOptimizer(qaoa_mes)  # using QAOA
    final_qaoa_result = qaoa.solve(qubo)
    optimal_p = 1
    optimal_init = [0.,0.]
    time = final_qaoa_result.min_eigen_solver_result.optimizer_time
    ###
    
    min_sol, min_fval, min_prob, min_rank, _ = results_from_QAOA(final_qaoa_result)


    for p in p_list:
        grid_init = [np.random.normal(1, 1, p * 2) for i in range(n_init)]

        it, min_fval = 0, 0
        for init in grid_init:
            it = it + 1
            # print(it)
            qaoa_mes = QAOA(optimizer=optimizer, reps=p,
                            quantum_instance=backend, initial_point=init)

            qaoa = MinimumEigenOptimizer(qaoa_mes)  # using QAOA
            qaoa_result = qaoa.solve(qubo)

            _, fval, _, rank, _ = results_from_QAOA(qaoa_result)

            if (min_fval>fval) and (rank<min_rank):
                min_fval = fval
                min_rank = rank

                optimal_p = p
                optimal_init = init
                final_qaoa_result = qaoa_result
                
                time = final_qaoa_result.min_eigen_solver_result.optimizer_time

    return final_qaoa_result, optimal_p, optimal_init, time