(ns libapl-clj.impl.jna
  (:require [tech.v3.jna :as jna]
            [tech.v3.jna.base :as jna-base]
            [tech.v3.datatype.casting :as casting]
            [tech.v3.datatype.pprint :as dtype-pp]
            [camel-snake-kebab.core :as csk]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.tools.logging :as log]
            [environ.core :as environ])
  (:import [com.sun.jna Pointer NativeLibrary]))

(defonce apl-library-path* (atom (or (environ/env "LD_LIBRARY_PATH")
                                     "/usr/local/lib/apl")))

(defonce _ (jna/add-library-path "libapl.so" :resource  @apl-library-path*))

(defn apl-library-path []
  (str @apl-library-path* "/libapl.so"))

(defn find-apl-function [fn-name]
  (jna/find-function fn-name (apl-library-path)))


;; todo -- not sure if either of these is correct


(defn apl_value
  ^Pointer [item]
  (jna/ensure-ptr item))

;; todo -- see previous
(def APL_value (find-apl-function "apl_value"))

(jna/def-jna-fn (apl-library-path)
  init_libapl
  "Initialize GNU APL!"
  nil
  [progname str]
  [logname int])

(jna/def-jna-fn (apl-library-path)
  apl_exec
  "Run an APL command!"
  Integer
  [line str])

(defn initialize!
  []
  ;; todo -- what are the purpose of these arguments?
  (init_libapl "apl" 1))

(defn run-simple-string! [cmd]
  (apl_exec cmd))

(jna/def-jna-fn (apl-library-path)
  get_var_value
  "Get APL variable value"
  ;; todo -- what should this value be?
  apl_value
  [var_name str]
  [loc str])

(comment
  (initialize!)
  (run-simple-string! "res ← 4 4 ⍴ 3") ;; works!
  (get_var_value "res" 0) ;; doesn't work!
  )
