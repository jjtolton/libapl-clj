(ns libapl-clj.apl
  (:require [libapl-clj.impl.jna :as jna]))

(defn initialize!
  "Initialize the APL shared library.  Currently only tested on Linux."
  []
  (jna/init_libapl "apl" 1))

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
  (def res (apl-value-pointer "res"))
  res
  (pointer->string res)
  (pointer->rank res)
  (pointer->count res)

  (run-simple-string! "res1 ← res + res")
  (def res1 (apl-value-pointer "res1"))
  res
  (pointer->string res1)
  (pointer->rank res1)
  (pointer->count res1))
