(ns ^{:doc "This is a low level API one level above 
the JNA bindings to provide a thin layer of indirection.  

Contributors: Please use this rather than than the JNA bindings for 
            : manipulating APL memory space.

Application developers: Please be careful hooking into this directly.
                      : While more stable than the JNA bindings,
                      : the API is subject to change.


overview
--------
No type coercion.  Simply an abstraction
from the underlying JNA bindings in case we want to switch
to a different runtime down the road, such as Dyalog, for instance, 
or to potentially provide hooks.  

The only conveniences provided are:

* the `loc` argument is omitted, as it is for debugging C code.
* the API is made more idiomatic than raw JNA interop

variable conventions
--------------------

avp -- APL value pointer. \"Opaque\" pointer reference to an APL array or scalar,
     : expressed in Clojure as a JNA Pointer.

atp -- APL value pointer that specifically an APL Tensor (aka NDarray, Vector, or 
     : multi-dimensional array).

asp -- APL value pointer that specifically maps to an APL Scalar.

fp --  APL function pointer. \"Opaque\" pointer reference to an APL function.
    :  the \"a\" is dropped from \"afp\" to provide a visual distinction from \"avp\".

op --  APL \"operation\" pointer.  We refer to APL \"operations\" as higher order functions
    :  in Clojure. An example is `reduce`, which takes a function as an argument.
    :  In APL, reduce (given the `/` symbol) takes a function as its left argument.


s  --  a string

ax, axis -- an integer representing the index of a value in the shape of a Tensor
          : i.e., if the shape is [2 3 5], axis 0 is 2, axis 1 is 3, axis 2 is 5  

p  -- a pointer

vref -- a string, the variable name associate with an APL value

idx -- index


function naming conventions
---------------------------

x+y -- brutalist naming convention.  avp+idx->char indicates a function 
     : that takes an avp and an idx and returns a char.
     : avp+fp+avp->avp represents a function that has parameters [avp fp avp] and returns
     : an avp.  etc.


"}
    libapl-clj.impl.api
  (:require [libapl-clj.impl.jna :as jna :reload true]
            [tech.v3.datatype :as dtype])
  (:import [com.sun.jna Pointer]))

(defn initialize! []
  ;; todo -- add initialization options, such as library location
  (jna/init_libapl "main" 1))

(defn run-simple-string! ^Integer [^String s]
  (jna/apl_exec s))

(defn run-command-string! ^Integer [^String cmd & args]
  ;; todo -- important to understand GNU APL command context better
  ;;         to make this more useful
  (jna/apl_command ^String (clojure.string/join  (into [cmd] args))))

(defn ^:private result-callback
  [result committed]
  ;; todo -- what does this do...?
  (jna/result_callback result committed))

(defn pointer->string ^String [^Pointer avp]
  (jna/print_value_to_string avp))

(defn rank ^Integer [^Pointer avp]
  (jna/get_rank avp))

(defn axis ^Integer [^Pointer avp ^Integer ax]
  (jna/get_axis avp ax))

(defn ref->avp  ^Pointer [^String vref]
  (jna/get_var_value vref ""))

(defn atp->ref!
  "Warning: crash if you attempt to assign a scalar pointer"
  [^String ref ^Pointer atp]
  (jna/set_var_value ref atp ""))

(defn avp->string! ^String [^Pointer avp]
  (jna/print_value_to_string avp))

(defn n-elems ^Integer [^Pointer avp]
  (jna/get_element_count avp))

(comment
  (n-elems (char-vector "hello"))
  (n-elems (int-scalar 1))
  (n-elems (double-scalar 10.12))
  (n-elems (complex-scalar 1 2))
  
  )

(defn type-at-idx
  "Get type of value for pointer at idx.
   CT_NONE    = 0,
   CT_BASE    = 0x01,
   CT_CHAR    = 0x02,
   CT_POINTER = 0x04,
   CT_CELLREF = 0x08,
   CT_INT     = 0x10,
   CT_FLOAT   = 0x20,
   CT_COMPLEX = 0x40,
   CT_NUMERIC = CT_INT  | CT_FLOAT   | CT_COMPLEX,
   CT_SIMPLE  = CT_CHAR | CT_NUMERIC,
   CT_MASK    = CT_CHAR | CT_NUMERIC | CT_POINTER | CT_CELLREF"
  ^Integer [^Pointer avp ^Integer idx]
  (jna/get_type avp idx))

(defn atp+idx->avp ^Pointer [^Pointer atp ^Integer idx]
  (jna/get_value atp idx))

(def avp< atp+idx->avp)

(defn atp+idx->int  ^Integer [^Pointer atp ^Integer idx]
  (jna/get_int atp idx))

(def int< atp+idx->int)

(defn atp+idx->real ^Double [^Pointer atp ^Integer idx]
  (jna/get_real atp idx))

(def real< atp+idx->real)

(defn atp+idx->imag ^Double [^Pointer atp ^Integer idx]
  (jna/get_imag atp idx))

(def imag< atp+idx->imag)

(defn atp+idx->char ^Character [^Pointer atp ^Integer idx]
  (jna/get_char atp idx))

(def char< atp+idx->char)

(defn set-char! [^Pointer atp ^Integer idx ^Character c]
  (jna/set_char c atp idx))

(defn set-value! [^Pointer atp ^Integer idx ^Pointer avp]
   (jna/set_value avp atp idx))

(defn set-int! [^Pointer atp ^Integer idx ^Integer n]
  (jna/set_int n atp idx))

(defn set-double! [^Pointer atp ^Integer idx ^Double n]
  (jna/set_double n atp idx))

(defn set-complex! [^Pointer atp ^Integer idx ^Double real ^Double imag]
  (jna/set_complex real imag atp idx))

;; constructors

(defn apl-value
  "Another exception to the 'no casting' rule.  I can't see 
any reasion to not do the casting to a container here."
  ^Pointer [shape]
  (jna/apl_value (count shape)
                 (dtype/make-container :native-heap :int64 shape)
                 ""))

(defn apl-scalar ^Pointer []
  (jna/apl_scalar ""))

(defn apl-vector ^Pointer [^Integer length]
  (jna/apl_vector length ""))

(defn apl-cube ^Pointer [^Integer blocks ^Integer rows ^Integer cols]
  (jna/apl_cube blocks rows cols ""))

(defn char-vector ^Pointer [^String string]
  (jna/char_vector string ""))

(defn int-scalar ^Pointer [^Long n]
  (jna/int_scalar n ""))

(defn double-scalar ^Pointer [^Double n]
  (jna/double_scalar n ""))

(defn complex-scalar ^Pointer [^Pointer real ^Float imag]
  (jna/complex_scalar real imag ""))

(defn char-scalar ^Pointer [^Character c]
  (jna/char_scalar c))

;; destructors

(defn release-value! [^Pointer avp]
  (jna/release_value avp ""))

;; auxiliary

(defn owner-count [^Pointer avp]
  (jna/get_owner_count avp))


(defn ->string-container
  ([] (dtype/make-container :native-heap :uint64 []))
  ([s]
   (if (nil? s)
     (->string-container)
     (->> s
          str
          vec
          first
          vector
          (dtype/make-container :native-heap :uint64)))))

(defn get-function-ucs
  "Note: this function is an exception to the 'no-coercion' rule for this
namespace because this is an esoteric technique and I can't think of a valid
reason not to do it here."
  ([^String fstring]
   (get-function-ucs fstring nil nil))
  ([^String s1 ^String s2]
   (get-function-ucs s1 s2 nil))
  ([s1 s2 s3]
   (jna/get_function_ucs (->string-container s1)
                         (->string-container s2)
                         (->string-container s3))))

(comment

  (get-function-ucs "⍴"))

(defn niladic-fp ^Pointer [^Pointer fp]
  (jna/eval__fun fp))

(defn arg+fp+arg
  "Note that order of arguments is more idiomatic to APL than Clojure"
  ^Pointer
  [arg1 ^Pointer fp arg2]
  (jna/eval__A_fun_B arg1 fp arg2))

(defn arg+fp+axis+arg ^Pointer [arg1 ^Pointer fp ^Pointer axis arg2]
  (jna/eval__A_fun_X_B arg1 fp axis arg2))

(defn arg+fn+op+axis+arg ^Pointer [arg1 ^Pointer fp ^Pointer op ^Pointer axis arg2]
  (jna/eval__A_L_oper_X_B arg1 fp op axis arg2))

(defn arg+fp+op+fp+arg ^Pointer [arg1 ^Pointer fp1 ^Pointer op ^Pointer fp2 ^Pointer arg2]
  (jna/eval__A_L_oper_R_B arg1 fp1 op fp2 arg2))

(defn arg+fn+op+fn+axis+arg ^Pointer
  [arg1
   ^Pointer fp1
   ^Pointer op
   ^Pointer fp2
   ^Pointer axis
   arg2]
  (jna/eval__A_L_oper_R_X_B arg1 fp1 op fp2 axis arg2))

(defn fp+arg ^Pointer [^Pointer fp arg]
  (jna/eval__fun_B fp arg))

(defn fp+op+arg ^Pointer [^Pointer fp ^Pointer op arg]
  (jna/eval__L_oper_B fp op arg))

(defn fp+op+axis+arg ^Pointer [^Pointer fp ^Pointer op ^Pointer axis arg]
  (jna/eval__L_oper_X_B fp op axis arg))

(defn fp+axis+arg ^Pointer [^Pointer fp ^Pointer axis arg]
  (jna/eval__fun_X_B fp axis arg))

(defn fp+op+fp+arg ^Pointer [^Pointer fp1 ^Pointer op ^Pointer fp2 arg]
  (jna/eval__L_oper_R_B fp1 op fp2 arg))

(defn fp+op+axis+arg ^Pointer [^Pointer fp ^Pointer op ^Pointer axis arg]
  (jna/eval__L_oper_X_B fp op axis arg))

(defn fp+op+fp+axis+arg ^Pointer [^Pointer fp1 ^Pointer op ^Pointer fp2 ^Pointer axis arg]
  (jna/eval__L_oper_R_X_B fp1 op fp2 axis arg))
