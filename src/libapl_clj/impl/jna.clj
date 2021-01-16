(ns libapl-clj.impl.jna
  (:require [tech.v3.jna :as jna]
            tech.v3.datatype.jna
            [environ.core :as environ]
            [tech.v3.datatype :as dtype])
  (:import [com.sun.jna Pointer]))

(def ^:private default-linux-library-path "/usr/local/lib/apl")

(defonce apl-library-path* (atom (or (environ/env "APL_LIBRARY_PATH")
                                     default-linux-library-path)))

(defn APL_value ^Pointer [value]
  (jna/ensure-ptr value))

(defn APL_function ^Pointer [fun]
  (jna/ensure-ptr fun))

(defn uint64_t* ^Pointer [value]
  (jna/ensure-ptr value))

(defn int64_t* ^Pointer [value]
  (jna/ensure-ptr value))

(defonce _ (jna/add-library-path "libapl.so" :resource  @apl-library-path*))

(defn apl-library-path []
  (str @apl-library-path* "/libapl.so"))

(defn find-apl-function [fn-name]
  (jna/find-function fn-name (apl-library-path)))

(defn find-apl-symbol ^Pointer [sym-name]
  (jna/find-function sym-name (apl-library-path)))

(defn find-deref-apl-symbol ^Pointer [sym-name]
  (-> (find-apl-symbol sym-name)
      (.getPointer 0)))


;; initialization

(jna/def-jna-fn (apl-library-path)
  init_libapl
  "Initialize GNU APL!"
  nil
  [progname str]
  [logname int])

;; execute

(jna/def-jna-fn (apl-library-path)
  apl_exec
  "Run an APL string!"
  Integer
  [line str])

(jna/def-jna-fn (apl-library-path)
  apl_command
  "Run an APL command"
  Integer
  [line str])

(jna/def-jna-fn (apl-library-path)
  result_callback
  "A function called with the final value of 
an APL statement."
  Pointer
  [result APL_value]
  [committed int])


(jna/def-jna-fn (apl-library-path)
  print_value_to_string
  "Convert opaque APL value to string"
  String
  [value APL_value])

;; selectors

(jna/def-jna-fn (apl-library-path)
  get_var_value
  "Get APL variable value"
  Pointer
  [var_name str]
  [loc str])

(jna/def-jna-fn (apl-library-path)
  get_axis
  "Get axis of val"
  Long
  [val APL_value]
  [axis int])



;; primary introspection API of APL Values

(jna/def-jna-fn (apl-library-path)
  get_rank
  "Get Rank of APL tensor"
  Integer
  [value APL_value])

(jna/def-jna-fn (apl-library-path)
  get_element_count
  "Get number of elements of APL value"
  Integer
  [value APL_value])

;; cell accessors

(jna/def-jna-fn (apl-library-path)
  get_type
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
  Integer
  [value APL_value]
  [idx unchecked-int])

(jna/def-jna-fn (apl-library-path)
  get_value
  "Get value at index -- used for objects or nested arrays"
  Pointer
  [value APL_value]
  [idx unchecked-int])

(jna/def-jna-fn (apl-library-path)
  get_int
  "Get integer for pointer at index of val"
  Integer
  [value APL_value]
  [idx unchecked-int])

(jna/def-jna-fn (apl-library-path)
  get_real
  "Get double value for pointer at index of val, or real part of complex number"
  Double
  [value APL_value]
  [idx unchecked-int])

(jna/def-jna-fn (apl-library-path)
  get_imag
  "Get imaginary part of complex number at index of val"
  Double
  [val APL_value]
  [idx unchecked-int])

(jna/def-jna-fn (apl-library-path)
  get_char
  "Get characater at index of val"
  Character
  [val APL_value]
  [idx unchecked-int]
  )

;; setters
(jna/def-jna-fn (apl-library-path)
  set_value
  "Set value of value at index"
  Pointer
  [new_value APL_value]
  [val APL_value]
  [idx unchecked-int])

(jna/def-jna-fn (apl-library-path)
  set_var_value
  "Set value to variable"
  Integer
  [var_name str]
  [new_value APL_value]
  [loc str])

(jna/def-jna-fn (apl-library-path)
  set_char
  "Set value at idx to char"
  nil
  [new_char char]
  [val APL_value]
  [idx unchecked-int])

(jna/def-jna-fn (apl-library-path)
  set_int
  "Set value at idx to int"
  nil
  [new_int long]
  [val APL_value]
  [idx unchecked-int])

(jna/def-jna-fn (apl-library-path)
  set_double
  "Set value at idx to double"
  nil
  [new_double double]
  [val APL_value]
  [idx unchecked-int])

(jna/def-jna-fn (apl-library-path)
  set_complex
  "Set value at idx to complex"
  nil
  [new_real double]
  [new_imag double]
  [val APL_value]
  [idx unchecked-int])


;; constructors
(jna/def-jna-fn (apl-library-path)
  apl_value
  "Create new APL vector"
  Pointer
  [rank int]
  [shape jna/ensure-ptr]
  [loc str])

(jna/def-jna-fn (apl-library-path)
  apl_scalar
  "Create new APL scalar"
  Pointer
  [loc str])

(jna/def-jna-fn (apl-library-path)
  apl_vector
  "A new vector with a ravel of length len
and ravel elements initialized to zero"
  Pointer
  [len long]
  [loc str])


(jna/def-jna-fn (apl-library-path)
  apl_matrix
  "a new matrix. all ravel elements are initialized to integer 0"
  Pointer
  [rows long]
  [cols long]
  [loc str])

(jna/def-jna-fn (apl-library-path)
  apl_cube
  "a new 3-dimensional value. all ravel elements are initialized to integer 0"
  Pointer
  [blocks long]
  [rows long]
  [cols long]
  [loc str])

(jna/def-jna-fn (apl-library-path)
  char_vector
  "Create a new character vector"
  Pointer
  [string str]
  [loc str])

(jna/def-jna-fn (apl-library-path)
  int_scalar
  "A new integer scalar"
  Pointer
  [val long]
  [loc str])

(jna/def-jna-fn (apl-library-path)
  double_scalar
  "A new floating point scalar"
  Pointer
  [val double]
  [loc str])

(jna/def-jna-fn (apl-library-path)
  complex_scalar
  "A new complex scalar"
  Pointer
  [real float]
  [imag float]
  [loc str])

(jna/def-jna-fn (apl-library-path)
  char_scalar
  "A new character scalar"
  Pointer
  [unicode char]
  [loc str])


;; destructor

(jna/def-jna-fn (apl-library-path)
  release_value
  "APL value destructor function. All non-0 APL_values must be released
   at some point in time (even const ones). release_value(0) is not needed
   but accepted."
  nil
  [val APL_value]
  [loc str])

(jna/def-jna-fn (apl-library-path)
  get_owner_count
  "return the number of owners of value val. An owner count of 1 means that
   the next release_value(val) will delete the value val. If the APL
   command )CHECK reports \"stale values\" then they might be caused by
   missing calls of release_value(); in this case get_owner_count() can
   be used to trouble-shoot the cause for stale values."
  Pointer
  [val APL_value])

(jna/def-jna-fn (apl-library-path)
  get_function_ucs
  "Get reference to an APL function.

Example:
(require '[tech.v3.jna :as jna]
          'tech.v3.datatype.jna)
(def ⍴ (jna/get_function_ucs (dtype/make-container :native-heap :uint64 [\\⍴])
                             (dtype/make-container :native-heap :uint64 [])
                             (dtype/make-container :native-heap :uint64 [])))

Note that the '\\⍴' is a char, and is double-escaped in the docstring
"
  Pointer
  [name jna/ensure-ptr]
  [L jna/ensure-ptr]
  [R jna/ensure-ptr])

(jna/def-jna-fn (apl-library-path)
  eval__fun
  "evaluate niladic function f"
  Pointer
  [fun APL_function])

(jna/def-jna-fn (apl-library-path)
  eval__A_fun_B
  "dyadic function fun with arguments A and B

  (let [A (-> [3 3] tensor/->tensor ->apl)
        B (jna/char_vector \"hello\" \"\")]
    (drawing (jna/eval__A_fun_B A ⍴ B)))

┏→━━┓
↓hel┃
┃loh┃
┃ell┃
┗━━━┛

"
  Pointer
  [A APL_value]
  [fun APL_function]
  [B APL_value])

(jna/def-jna-fn (apl-library-path)
  eval__A_L_oper_B
  "monadic operator oper with function L arguments A and B"
  Pointer
  [A APL_value]
  [L APL_function]
  [fun APL_function]
  [B APL_value])

(jna/def-jna-fn (apl-library-path)
  eval__A_fun_X_B
  "dyadic function fun with axis X and arguments A and B"
  Pointer
  [A APL_value]
  [fun APL_function]
  [X APL_value]
  [B APL_value])

(jna/def-jna-fn (apl-library-path)
  eval__A_L_oper_R_B
  "dyadic operator oper with functions L and R and arguments A and B"
  Pointer
  [A APL_value]
  [L APL_function]
  [fun APL_function]
  [R APL_function]
  [B APL_value])

(jna/def-jna-fn (apl-library-path)
  eval__A_L_oper_X_B
  "monadic operator oper with functions L, axis X, and arguments A and B"
  Pointer
  [A APL_value]
  [L APL_function]
  [fun APL_function]
  [X APL_value]
  [B APL_value])

(jna/def-jna-fn (apl-library-path)
  eval__A_L_oper_R_X_B
  "dyadic operator oper with functions L and R, axis X, and arguments A and B"
  Pointer
  [A APL_value]
  [L APL_function]
  [fun APL_function]
  [X APL_value]
  [B APL_value])


(jna/def-jna-fn (apl-library-path)
  eval__fun_B
  "monadic function fun with argument B"
  Pointer
  [fun APL_function]
  [B APL_value])

(jna/def-jna-fn (apl-library-path)
  eval__L_oper_B
  "monadic operator oper with function L argument B"
  Pointer
  [L APL_function]
  [fun APL_function]
  [B APL_function])

(jna/def-jna-fn (apl-library-path)
  eval__fun_X_B
  "monadic function fun with axis X and argument B"
  Pointer
  [fun APL_function]
  [X APL_value]
  [B APL_value])

(jna/def-jna-fn (apl-library-path)
  eval__L_oper_R_B
  "dyadic operator oper with functions L and R and argument B"
  Pointer
  [L APL_function]
  [fun APL_function]
  [X APL_function]
  [B APL_value])

(jna/def-jna-fn (apl-library-path)
  eval__L_oper_X_B
  "monadic operator oper with function L, axis X, and argument B"
  Pointer
  [L APL_function]
  [fun APL_function]
  [X APL_value]
  [B APL_value])

(jna/def-jna-fn (apl-library-path)
  eval__L_oper_R_X_B
  "dyadic operator oper with functions L and R, axis X, and argument B"
  Pointer
  [L APL_function]
  [fun APL_function]
  [R APL_function]
  [X APL_value]
  [B APL_value])
