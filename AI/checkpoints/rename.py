import os
 
# Function to rename multiple files
def main():
    files = os.listdir(".")
    for filename in files:
        if filename.startswith("Transformer"):		
            print(filename)
            src=filename
            dest=filename.replace('mean','43')
            os.rename(src, dest)
 
# Driver Code
if __name__ == '__main__':
     
# Calling main() function
    main()
