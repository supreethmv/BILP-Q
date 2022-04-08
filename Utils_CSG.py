#!/usr/bin/env python
# coding: utf-8

import math
import numpy as np
import pandas as pd
from sympy import *
import re
import time
import random
import itertools

################### ########
#Different distributions data generator functions

"""
All different distributions considered as benchmark for the evaluation of the State of the art
Coalition Structure Generationn Problem solver, i.e., A Bi-directional Search Technique for 
Optimal Coalition Structure Generation with Minimal Overlapping has been considered below.

The below functions are generating the coalition values which are in the distribution of the 
corresponding function names.
"""

def Agent_based_uniform(y,Power,Numofagent):
  strx = bin(y)[2:]
  length = len(strx)
  Tempcoal = []
  for x in range(0, length):
      if (int(strx[x]) == 1):
          Tempcoal.append(length - x)
  sums = 0
  for j in Tempcoal :
      partial = np.random.uniform(0, 2 * Power.get(j))
      sums = sums + partial
  return sums


def Agent_based_normal(y,Power,Numofagent):
  strx = bin(y)[2:]
  length = len(strx)
  Tempcoal = []
  for x in range(0, length):
      if (int(strx[x]) == 1):
          Tempcoal.append(length - x)
  sums = 0
  for j in Tempcoal:
      partial = np.random.normal(Power.get(j), 0.01)
      sums = sums + partial
  return sums


def Beta_distribution(y,Power,Numofagent):
    return bin(y).count('1')* np.random.beta(.5, .5)


def Modified_normal_distribution(y,Power,Numofagent):
  mu = 10 * bin(y).count('1')
  sigma = 0.01
  value = np.random.normal(mu, sigma)
  randval = np.random.uniform(0, 50)
  if randval <= 20:
      value = value + randval
  return value


def Modified_uniform_distribution(y,Power,Numofagent):
  value= np.random.uniform(0,10*bin(y).count('1'))
  randval = np.random.uniform(0, 50)
  if randval <= 20:
      value = value + randval
  return  value


def Normal_distribution(y,Power,Numofagent):
  mu = 10 * bin(y).count('1')
  sigma = 0.01
  return np.random.normal(mu, sigma)


def SVA_BETA_distribution(y,Power,Numofagent):
  strx = bin(y)[2:]
  length = len(strx)
  Tempcoal = []
  for x in range(0, length):
      if (int(strx[x]) == 1):
          Tempcoal.append(length - x)
  x = len(Tempcoal)
  inputx = x * np.random.beta(0.5, 0.5)
  if (strx[len(strx) - 1] == str(1)):
      inputx = 200 + inputx
  return inputx


def Weibull_distribution(y,Power,Numofagent):
  lengthofcoal = bin(y).count('1')
  value = np.random.weibull(lengthofcoal * lengthofcoal)
  return value


def Rayleigh_distribution(y,Power,Numofagent):
  lengthofcoal = bin(y).count('1')
  mu = 10 * lengthofcoal
  modevalue = np.sqrt(2 / np.pi) * mu
  value = np.random.rayleigh(modevalue)
  return value


def Weighted_random_with_chisquare(y,Power,Numofagent):
  lengthofcoal = bin(y).count('1')
  value = random.randint(1, lengthofcoal)
  inputx = lengthofcoal * np.random.chisquare(lengthofcoal)
  totalvalue = value + inputx
  return totalvalue


def F_distribution(y,Power,Numofagent):
  lengthofcoal = bin(y).count('1')
  dfden = lengthofcoal + 1
  dfnum = 1
  value = np.random.f(dfnum, dfden)
  return value


def Laplace_or_double_exponential(y,Power,Numofagent):
  lengthofcoal = bin(y).count('1')
  value = np.random.laplace(10 * lengthofcoal, 0.1)
  return value


def generate_problem_instance(distribution, NumAgent):
    totalcoalitions = 2 ** NumAgent
    y = 1
    Agents = list(range(1, NumAgent + 1))
    RealAgent = Agents[0:NumAgent]
    Power = {}
    for i in RealAgent:
        Power[i] = np.random.uniform(0, 10)
    coalition_values = {}
    while (y < totalcoalitions):
        sums = float("{0:.3f}".format(distribution(y,Power,NumAgent)))
        coalition_values[','.join([str(idx+1) for idx,bit in enumerate(bin(y)[2:][::-1]) if int(bit)])] = sums
        y = y + 1
    return coalition_values

##########################################



###########################################


def convert_to_BILP(coalition_values):
  """
  convert_to_BILP formulates the BILP problem for a given CSG problem instance
  :param coalition_values: dictionary of game/problem instance with key as the coalitions and value as coalition values
                          Also called the Characteristic function
  :return: tuple of (c,S,b) where c is the coalition values, S is a the binary 2D array and b is a binary vector
  """ 
  x={}
  for i in range(len(coalition_values)):
    x[i] = symbols(f'x_{i}')
  n = list(coalition_values.keys())[-1].count(',')+1                              #get number of agents
  S=[]
  for agent in range(n):
    temp = []
    for coalition in coalition_values.keys():
      if str(agent+1) in coalition:
        temp.append(1)                                                            #'1' if agent is present in coalition
      else:
        temp.append(0)                                                            #'0' if agent is not present in coalition
    S.append(temp)
  b=[1]*n                                                                         # vector b is a unit vector in case of BILP of CSGP
  c = list(coalition_values.values())                                             # vector of all costs (coalition values)
  return (c,S,b)


def get_QUBO_coeffs(c,S,b,P):
  """
  get_QUBO_coeffs converts the BILP problem instace into linear and quadratic terms required for QUBO formulation
  :param c: list of coalition values
         S: a binary 2D array
         b: a binary vector
         P: Penalty coefficient for the constraints of BILP problem to convert into an unconstrained QUBO problem
  :return linear: dictionary of linear coefficient terms
          quadratic: dictionary of quadratic coefficient terms
  """
  x={}
  for i in range(len(c)):
    x[i] = symbols(f'x_{i}')
  final_eq = simplify(sum([c_value*x[i] for i,c_value in enumerate(c)])+P*sum([expand((sum([x[idx] for idx,element in enumerate(agent) if element])-1)**2) for agent in S])) #simplify numerical equation
  linear={}
  quadratic={}
  for term in final_eq.as_coeff_add()[1]:
    term = str(term)
    if '**' in term:                                                              #get the coefficient of the squared terms as linear coefficients (diagonal elements of the Q matrix in the QUBO problem)
      if term.count('*')==3:
        linear[term.split('*')[1]] = float(term.split('*')[0])
      else:
        if not term.startswith('-'):
          linear[term.split('**')[0]] = float(1)
        else:
          linear[term.split('**')[0][1:]] = float(-1)
    elif term.count('*')==1:                                                        #get the coefficient of the linear terms (diagonal elements of the Q matrix in the QUBO problem)
      linear[term.split('*')[1]] += float(term.split('*')[0])
    else:
      quadratic[(term.split('*')[1],term.split('*')[2])] = float(term.split('*')[0])  #get the coefficient of quadratic terms (upper diagonal elements of the Q matrix of the QUBO problem)  
  linear = {k:-v for (k,v) in linear.items()}
  #{k: abs(v) for k, v in D.items()}
  quadratic = {k:-v for (k,v) in quadratic.items()}
  return linear,quadratic





def natural_keys(text):
  """
  alist.sort(key=natural_keys) sorts in human order
  http://nedbatchelder.com/blog/200712/human_sorting.ht
  For example: Built-in function ['x_8','x_10','x_1'].sort() will sort as ['x_1', 'x_10', 'x_8']
  But using natural_keys as callback function for sort() will sort as ['x_1','x_8','x_10']
  """
  return [ atoi(c) for c in re.split(r'(\d+)', text) ]


def atoi(text):
  """
  Function returns the corresponding value of a numerical string as integer datatype
  """
  return int(text) if text.isdigit() else text







######################## DECODING

#convert solution binary string to coalition structure and also output_ the coalition
def decode(solution,coalition_values):
  """
  Convert the solution binary string into a coalition sructure(a list of coalitions)
  """
  output = []
  for index,element in enumerate(solution):
    if int(element)!=0:
      output.append(set(list(coalition_values)[index].split(',')))
  return output



def get_linear_quads(distribution,n, qubo_penalty = 50):
    """
    wrapper function to fetch the linear and quadratic terms for a given distribution and number of agents
    """
    coalition_values = generate_problem_instance(distribution, n)
    c,S,b = convert_to_BILP(coalition_values)
    qubo_penalty = qubo_penalty*-1                                                #lambda is a negative constant
    linear,quadratic = get_QUBO_coeffs(c,S,b,qubo_penalty)
    return coalition_values,linear,quadratic





#Classical BILP solver

def constraint(x,S):
  """
  constraint(x,S) returns True if x is a valid binary string, else False
  """
  for i in range(len(S)):
    temp = 0
    for j in range(len(x)):
      temp+= (S[i][j]*int(x[j]))
    if temp!=1:
      return False
  return True


def solve_BILP_classical(coalition_values):
  """
  solve_BILP_classical() Evaluates the coalition value for all possible valid binary strings
  """
  S=[]
  for i in range(math.ceil(math.sqrt(len(coalition_values)))):                    #get number of agents
    S.append([])
    for j,value in coalition_values.items():
      if str(i+1) in j:
        S[i].append(1)
      else:
        S[i].append(0)
  optimal_cost = 0
  for b in range(2**len(coalition_values)):
    x = [int(t) for t in reversed(list(bin(b)[2:].zfill(len(coalition_values))))]
    x = ''.join(str(e) for e in x)
    if constraint(x,S):
      cost = 0
      for j in range(len(x)):
        cost+=coalition_values[list(coalition_values.keys())[j]]*int(x[j])
      if optimal_cost<cost:
        optimal_cost = cost
        optimal_x = x
  return optimal_x


