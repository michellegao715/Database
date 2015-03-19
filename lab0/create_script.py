import sys
from sys import argv

# usage: 
#python create_script.py part-physician.csv part_physician_script.sql PartPhysician 

if __name__ == '__main__':
  f = open(argv[1], 'r')
  table_name = argv[3] 
  firstline, secondline = '',''
  new_line = ''
  script = ''
  for i, line in enumerate(f.readlines()):
    if i == 0:
      firstline = line  
      attr_list = firstline.split(',')
      for l in attr_list[:len(attr_list)-1]:
        l = l[1:len(l)-1]
        l += ' TEXT, '
        new_line += l
      # add last line into script.sql
      l = attr_list[len(attr_list)-1]
      l = l[1:len(l)-2]
      l += ' TEXT' 
      new_line += l
      script += 'CREATE TABLE '+table_name+ ' ('+new_line+');'
      print 'create table script '
      print script
      #new_line += '\n' #add a new line between attributes and following rows
    else:  
      script += '\n'
      new_line = '' # clear new_line before adding each row 
      secondline = line
      val_list = secondline.split(',')
      for l in val_list[:len(val_list)-1]:
        l = l[1:len(l)-1]
        l += ' , '
        new_line += l
      l = val_list[len(val_list)-1]
      l = l[1:len(l)-2]
      new_line += l
      script += 'INSERT INTO '+table_name+' VALUES ('+new_line+');'
      #new_line += '\n'
  # truncate the file to zero length before opening. 
  new_f = open(argv[2], 'w')
  #new_f.write(new_line[:len(new_line)])
  new_f.write(script)
  new_f.close()



