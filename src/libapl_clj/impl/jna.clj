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

(jna/def-jna-fn (apl-library-path)
  init_libapl
  "Initialize GNU APL!"
  ;; (maybe?)
  nil)

(defn initialize!
  "Crash and burn!" ;; todo -- make this work
  []
  (init_libapl))

(comment
  (initialize!))
