
# fixes ordered lists in codox-generated HTML for specific files

import os

os.chdir('..')
htmlFiles = ['Adding_Genetic_Operators.html', 'Adding_Problem.html', 'Adding_Selection_Method.html']


for file in htmlFiles:

    with open('docs/'+file, 'r') as f:
        OL = "ol>"
        countOL = 0
        newline = []
        for line in f.readlines():
            if OL in line:
                countOL = countOL + 1
                if countOL != 2 and countOL != 3 and countOL != 6 and countOL != 7:
                    newline.append(line)
            else:
                newline.append(line)

    with open('docs/'+file, 'w') as f:
        for line in newline:
            f.writelines(line)

