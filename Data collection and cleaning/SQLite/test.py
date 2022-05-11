import csv
with open("data\movies.csv") as csv_file:
    csv_reader = csv.reader(csv_file, delimiter = ",")
    for row in csv_reader:
        print(row[0])
        print(row[1])
        print(row[2])
        print(len(row))