(ns libapl-clj.impl.jna
  (:require [tech.v3.jna :as jna]
            [environ.core :as environ])
  (:import [com.sun.jna Pointer]))

(def ^:private default-linux-library-path "/usr/local/lib/apl")

(defonce apl-library-path* (atom (or (environ/env "APL_LIBRARY_PATH")
                                     default-linux-library-path)))

(defn APL_value ^Pointer [value]
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

(jna/def-jna-fn (apl-library-path)
  init_libapl
  "Initialize GNU APL!"
  nil
  [progname str]
  [logname int])

(jna/def-jna-fn (apl-library-path)
  apl_exec
  "Run an APL string!"
  Integer
  [line str])

(jna/def-jna-fn (apl-library-path)
  get_var_value
  "Get APL variable value"
  Pointer
  [var_name str]
  [loc str])

(jna/def-jna-fn (apl-library-path)
  print_value_to_string
  "Convert opaque APL value to string"
  String
  [value APL_value])

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
