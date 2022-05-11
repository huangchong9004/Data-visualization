from math import log
import random
import numpy as np
def entropy(class_y):
        # Input:            
        #   class_y         : list of class labels (0's and 1's)

        # TODO: Compute the entropy for a list of classes
        #
        # Example:
        #    entropy([0,0,0,1,1,1,1,1,1]) = 0.9182958340544896
        # Do not round your result

        entropy = 0
        size = len(class_y)
        count = sum(class_y)
        m = count /size * 1.0
        n = 1 - m
        if (count == 0 or count == size):
            entropy = 0
        else:
            entropy = -m * log(m,2) - n * log(n, 2)
        return entropy
def information_gain(previous_y, current_y):
    # Inputs:
    #   previous_y: the distribution of original labels (0's and 1's)
    #   current_y:  the distribution of labels after splitting based on a particular
    #               split attribute and split value

    # TODO: Compute and return the information gain from partitioning the previous_y labels
    # into the current_y labels.
    # You will need to use the entropy function above to compute information gain
    # Reference: http://www.cs.cmu.edu/afs/cs.cmu.edu/academic/class/15381-s06/www/DTs.pdf

    """
    Example:

    previous_y = [0,0,0,1,1,1]
    current_y = [[0,0], [1,1,1,0]]

    info_gain = 0.4591479170272448
    """
    prev = entropy(previous_y)
    ratio = len(current_y[0])/len(previous_y)
    left = entropy(current_y[0])
    right = entropy(current_y[1])
    print(left, right)
    info_gain = np.float64((prev * len(previous_y) - (len(current_y[0]) * left + (len(previous_y) - len(current_y[0])) * right))/len(previous_y))
    
    return info_gain

y =[0,0,0,1,1,1,1,1,1]
previous_y = [0,0,0,1,1,1]
current_y = [[0,0], [1,1,1,0]]
print(information_gain(previous_y, current_y))
print(entropy(previous_y))
print(np.float64(entropy(current_y[1])))