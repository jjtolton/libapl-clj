(ns libapl-clj.apl
  (:require [tech.v3.jna :as jna]
            [tech.v3.jna.base :as jna-base]
            [tech.v3.datatype.casting :as casting]
            [tech.v3.datatype.pprint :as dtype-pp]
            [camel-snake-kebab.core :as csk]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.tools.logging :as log])
  (:import [com.sun.jna Pointer NativeLibrary]))






