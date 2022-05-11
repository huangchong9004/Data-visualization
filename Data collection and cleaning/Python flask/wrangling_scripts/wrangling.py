"""
cse6242 s22
wrangling.py - utilities to supply data to the templates.

This file contains a pair of functions for retrieving and manipulating data
that will be supplied to the template for generating the table. """
import csv
from json.tool import main

def username():
    return 'chuang440'

def data_wrangling():
    with open('data/movies.csv', 'r', encoding='utf-8') as f:
        reader = csv.reader(f)
        table = list()
        # Feel free to add any additional variables
        count = 0
        
        # Read in the header
        for header in reader:
            break
        
        # Read in each row
        for row in reader:
            table.append(row)
            
            # Only read first 100 data rows - [2 points] Q5.a
            count += 1
            if count == 100:
                break
        #print([table[i] for i in range(5)])
        #print(type(table[0][2]))
        # Order table by the last column - [3 points] Q5.b
        table.sort(key = lambda x: float(x[2]), reverse = True)
        print([table[i] for i in range(5)])
    return header, table

if __name__ == '__main__':
    data_wrangling()


