import numpy as np
import sys



args = np.array(sys.argv[1:])
in_arr = args.astype(int)
print(in_arr)
print(np.mean(in_arr))
