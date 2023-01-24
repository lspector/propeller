
# Takes Push instructions defined through (def-instruction and
# puts their documentation into a markdown file in docs_src

import os
from mdutils.mdutils import MdUtils
mdFile = MdUtils(file_name='src/docs_src/Additional_Instructions', title='Bool, Char, Code, Input-Output, Numeric, and String Instructions')

os.chdir('..')

instructionFiles = os.listdir('src/propeller/push/instructions')
print(instructionFiles)

hasDefInstruction = False
for file in instructionFiles:
    mdFile.new_header(level=1, title=file)
    try:
        print(file)

        # opening and reading the file
        file_read = open('src/propeller/push/instructions/'+file, "r")

        # set search text
        text = "(def-instruction"

        # reading file content line by line.
        lines = file_read.readlines()

        # looping through each line in the file
        # if the line contains "\(def-instruction", go through lines above that line and add
        # the Clojure comments to a list which is later written into markdown file.
        for count, line in enumerate(lines):
            new_list = []
            # print(line)
            # print(count)
            if text in line:
                hasDefInstruction = True
                # print(line)
                mdFile.new_header(level=2, title=lines[count+1].strip())
                isComment = True
                inc = 1
                while isComment:
                    if lines[count-inc].startswith(';;'):
                        new_list.append(lines[count-inc].replace(';', '').strip())
                        # print(lines[count-inc])
                        inc = inc + 1
                    else:
                        isComment = False
                        new_list.reverse()
                        for comment in new_list:
                            mdFile.write(comment+' ')
                functionInfo = lines[count+1].strip() + lines[count-1].replace(';', '').strip()
                # print(functionInfo)
                new_list.append(functionInfo)
        # closing file after reading
        file_read.close()

        # the input string doesn't
        # found in the text file
        if not hasDefInstruction:
            print("\n\"" + text + "\" is not found in \"" + file + "\"!")
        else:
            print("There is"+text)

    # entering except block
    # if input file doesn't exist
    except:
        print("\nThe file doesn't exist!")

mdFile.create_md_file()
