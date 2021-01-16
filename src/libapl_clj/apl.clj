(ns libapl-clj.apl
  (:require [libapl-clj.impl.jna :as jna :reload true]
            [libapl-clj.impl.pointer :as p :reload true]
            [tech.v3.tensor :as tensor]))

(defn initialize!
  "Initialize the APL shared library.  Currently only tested on Linux."
  []
  (jna/init_libapl "apl" 1)
  :ok)

(defn run-simple-string!
  "Run an APL command. Returns `true` if successful."
  [cmd]
  (zero? (jna/apl_exec cmd)))

(defn value-pointer
  "Get the pointer to APL value.  No marshalling has been 
implmented yet, so the only thing you can do right now is 
pass this value."
  [var-name]
  (jna/get_var_value var-name 0))

(defn pointer->string
  "Get the string representation of APL pointer"
  [p]
  (jna/print_value_to_string p))

(defn pointer->rank
  "Get the rank of an APL pointer"
  [p]
  (jna/get_rank p))

(defn pointer->count
  "Get the number of elements in an APL pointer"
  [p]
  (jna/get_element_count p))



(comment
  (initialize!)
  (run-simple-string! "res ← 4 4 ⍴ 3")
  (def res (value-pointer "res"))

  (long res)
  (vec (.getMethods (.getClass res)))
  (.getLong res)
  (long res)
  (java.beans.BeanInfo. res)
  (res )
  res
  (pointer->string res)
  (pointer->rank res)
  (pointer->count res)
  (jna/get_value res 0)

  (jna/get_type res 1)

  (tensor/nd-buffer-descriptor->tensor)

  (tech.v3.datatype/as-nd-buffer-descriptor
   res
   )
  


  
  (run-simple-string! "x ← 1.0 1.1 'a' 'hello' 'hey' 'b'")
  (def x (value-pointer "x"))
  x
  (pointer->string x)
  (= (jna/get_type x 0) 0x10 16) ;; integer
  (= (jna/get_type x 1) 0x20 32) ;; float
  (= (jna/get_type x 2) 0x02  2) ;; character
  (= (jna/get_type x 3) 0x04  4) ;; char point / string
  
  (run-simple-string! "x1 ← ⍳3")
  (def x1 (value-pointer "x1"))
  (jna/get_type x1 0)
  (jna/get_real x1 0)

  
  (run-simple-string! "res1 ← res + res")
  (def res1 (value-pointer "res1"))
  res
  (pointer->string res1)
  (pointer->rank res1)
  (pointer->count res1))
