



Table of contents
=================

* [Additional Instructions](#additional-instructions)
* [input_output.cljc](#input_outputcljc)
* [.DS_Store](#ds_store)
* [numeric.cljc](#numericcljc)
* [string.cljc](#stringcljc)
* [character.cljc](#charactercljc)
* [bool.cljc](#boolcljc)
* [code.cljc](#codecljc)

# Additional Instructions

# input_output.cljc

## :print_newline
Prints new line 
# .DS_Store

# numeric.cljc

## :float_cos
Pushes the cosine of the top FLOAT 
## :float_sin
Pushes the sine of the top FLOAT 
## :float_tan
Pushes the tangent of the top FLOAT 
## :float_from_integer
Pushes the floating point version of the top INTEGER 
## :integer_from_float
Pushes the result of truncating the top FLOAT towards negative infinity 
# string.cljc

## :string_butlast
Pushes the butlast of the top STRING (i.e. the string without its last letter) 
## :string_concat
Pushes the concatenation of the top two STRINGs (second + first) 
## :string_conj_char
Pushes the concatenation of the top STRING and the top CHAR (STRING + CHAR) 
## :string_contains
Pushes TRUE if the top STRING is a substring of the second STRING, and FALSE otherwise 
## :string_contains_char
Pushes TRUE if the top CHAR is contained in the top STRING, and FALSE otherwise 
## :string_drop
Pushes the top STRING with n characters dropped, where n is taken from the top of the INTEGER stack 
## :string_empty_string
Pushes TRUE if the top STRING is the empty string 
## :string_first
Pushes the first CHAR of the top STRING 
## :string_from_boolean
Pushes the STRING version of the top BOOLEAN, e.g. "true" 
## :string_from_char
Pushes the STRING version of the top CHAR, e.g. "a" 
## :string_from_float
Pushes the STRING version of the top FLOAT e.g. "2.05" 
## :string_from_integer
Pushes the STRING version of the top INTEGER, e.g. "3" 
## :string_indexof_char
Pushes the index of the top CHAR in the top STRING onto the INTEGER stack. If the top CHAR is not present in the top string, acts as a NOOP 
## :string_iterate
Iterates over the top STRING using code on the EXEC stack 
## :string_last
Pushes the last CHAR of the top STRING. If the string is empty, do nothing 
## :string_length
Pushes the length of the top STRING onto the INTEGER stack 
## :string_nth
Pushes the nth CHAR of the top STRING, where n is taken from the top of the INTEGER stack. If n exceeds the length of the string, it is reduced modulo the length of the string 
## :string_occurencesof_char
Pushes the number of times the top CHAR occurs in the top STRING onto the INTEGER stack 
## :string_parse_to_chars
Splits the top string into substrings of length 1 (i.e. into its component characters) and pushes them back onto the STRING stack in the same order 
## :string_remove_char
Pushes the top STRING, with all occurrences of the top CHAR removed 
## :string_replace
Pushes the third topmost STRING on stack, with all occurences of the second topmost STRING replaced by the top STRING 
## :string_replace_char
Pushes the top STRING, with all occurences of the second topmost CHAR replaced with the top CHAR 
## :string_replace_first
Pushes the third topmost STRING on stack, with the first occurence of the second topmost STRING replaced by the top STRING 
## :string_replace_first_char
Pushes the top STRING, with the first occurence of the second topmost CHAR replaced with the top CHAR 
## :string_rest
Pushes the rest of the top STRING (i.e. the string without its first letter) 
## :string_reverse
Pushes the reverse of the top STRING 
## :string_set_char
Pushes the top STRING, with the letter at index n (where n is taken from the INTEGER stack) replaced with the top CHAR. If n is out of bounds, it is reduced modulo the length of the string 
## :string_split
Splits the top STRING on whitespace, and pushes back the resulting components in the same order 
## :string_substr
Pushes the substring of the top STRING, with beginning and end indices determined by the second topmost and topmost INTEGERs respectively. If an index is out of bounds, the beginning/end of the string is used instead 
## :string_take
Pushes the substring of the top STRING consisting of its first n letters, where n is determined by the top INTEGER 
# character.cljc

## :char_is_letter
Pushes TRUE onto the BOOLEAN stack if the popped character is a letter 
## :char_is_digit
Pushes TRUE onto the BOOLEAN stack if the popped character is a digit 
## :char_is_whitespace
Pushes TRUE onto the BOOLEAN stack if the popped character is whitespace (newline, space, or tab) 
## :char_from_float
Pops the FLOAT stack, converts the top item to a whole number, and pushes its corresponding ASCII value onto the CHAR stack. Whole numbers larger than 128 will be reduced modulo 128. For instance, 248.45 will result in x being pushed. 
## :char_from_integer
Pops the INTEGER stack and pushes the top element's corresponding ASCII value onto the CHAR stack. Integers larger than 128 will be reduced modulo 128. For instance, 248 will result in x being pushed 
## :char_all_from_string
Pops the STRING stack and pushes the top element's constituent characters onto the CHAR stack, in order. For instance, "hello" will result in the top of the CHAR stack being \h \e \l \l \o 
# bool.cljc

## :boolean_and
Pushes the logical AND of the top two BOOLEANs 
## :boolean_or
Pushes the logical OR of the top two BOOLEANs 
## :boolean_not
Pushes the logical NOT of the top BOOLEAN 
## :boolean_xor
Pushes the logical XOR of the top two BOOLEAN 
## :boolean_invert_first_then_and
Pushes the logical AND of the top two BOOLEANs, after applying NOT to the first one 
## :boolean_invert_second_then_and
Pushes the logical AND of the top two BOOLEANs, after applying NOT to the second one 
## :boolean_from_float
Pushes FALSE if the top FLOAT is 0.0, and TRUE otherwise 
## :boolean_from_integer
Pushes FALSE if the top INTEGER is 0, and TRUE otherwise 
# code.cljc

## :code_append
Concatenates the top two instructions on the :code stack and pushes the result back onto the stack 
## :exec_do_range
Executes the top EXEC instruction (i.e. loops) a number of times determined by the top two INTEGERs, while also pushing the loop counter onto the INTEGER stack. The top INTEGER is the "destination index" and the second INTEGER is the "current index". If the integers are equal, then the current index is pushed onto the INTEGER stack and the code (which is the "body" of the loop) is pushed onto the EXEC stack for subsequent execution. If the integers are not equal, then the current index will still be pushed onto the INTEGER stack but two items will be pushed onto the EXEC stack - first a recursive call to :exec_do_range (with the same code and destination index, but with a current index that has been either incremented or decremented by 1 to be closer to the destination index) and then the body code. Note that the range is inclusive of both endpoints a call with integer arguments 3 and 5 will cause its body to be executed 3 times, with the loop counter having the values 3, 4, and 5. Note also that one can specify a loop that "counts down" by providing a destination index that is less than the specified current index. 
## :exec_do_count
Executes the top EXEC instruction (i.e. loops) a number of times determined by the top INTEGER, pushing an index (which runs from 0 to one less than the total number of iterations) onto the INTEGER stack prior to each execution of the loop body. If the top INTEGER argument is <= 0, this becomes a NOOP 
## :exec_do_times
Like :exec_do_count, but does not push the loop counter onto the INTEGER stack 
## :exec_if
If the top BOOLEAN is TRUE, removes the the second item on the EXEC stack, leaving the first item to be executed. Otherwise, removes the first item, leaving the second to be executed. Acts as a NOOP unless there are at least two items on the EXEC stack and one item on the BOOLEAN stack 
## :exec_when
If the top BOOLEAN is TRUE, leaves the first item on the EXEC stack to be executed. Otherwise, it removes it. Acts as a NOOP unless there is at least one item on the EXEC stack and one item on the BOOLEAN stack 
## :exec_while
Keeps executing the top instruction on the EXEC stack while the top item on the BOOLEAN stack is true 
## :exec_do_while
Keeps executing the top instruction on the EXEC stack while the top item on the BOOLEAN stack is true. Differs from :exec_while in that it executes the top instruction at least once 
## :exec_k
The "K combinator" - removes the second item on the EXEC stack 
## :exec_s
The "S combinator" - pops 3 items from the EXEC stack, which we will call A, B, and C (with A being the first one popped), and then pushes a list containing B and C back onto the EXEC stack, followed by another instance of C, followed by another instance of A 
## :exec_y
The "Y combinator" - inserts beneath the top item of the EXEC stack a new item of the form "(:exec_y TOP_ITEM)" 