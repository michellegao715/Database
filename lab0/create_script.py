import sys
from sys import argv

if __name__ == '__main__':
  f = open(argv[1], 'r')
  firstline = f.readline() 
  attr_list = firstline.split(',')
  new_line = ''
  
  for l in attr_list:
    l = l[1:len(l)-1]
    l += ' TEXT,' 
    new_line += l 
  
  
  print new_line[1:len(new_line)-1]
  new_f = open(argv[2], 'w')
  new_f.write(new_line[:len(new_line)-1])
  new_f.close()



