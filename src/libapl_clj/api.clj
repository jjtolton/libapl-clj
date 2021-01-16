(ns ^{:doc "A (more) stable API for working in APL memory space.  Coercion
from JVM->APL provided. This is the suggested API for library development."}
    libapl-clj.api
  (:require [libapl-clj.impl.api :as api]
            [libapl-clj.impl.ops :as ops]
            [libapl-clj.impl.helpers :as h]
            [libapl-clj.apl :as apl]
            [tech.v3.tensor :as tensor])
  (:import [com.sun.jna Pointer]))

(def initialize! ops/initialize!)

(def run-simple-string! ops/run-simple-string!)

(def run-command-string! api/run-command-string!)

(def ->string api/pointer->string)

(def rank api/rank)

(def axis api/axis)


;; could use some macros here
;; defint, defapl, defdouble, etc
;; maybe good first issue candidate

(def ref->apl api/ref->avp)

(defn set-apl-value!
  "Assign an APL Pointer to a variable name in APL space"
  [var-name apl-pointer]
  (if (h/scalar-pointer? apl-pointer)
    ;; todo -- this will crash on complex numbers.
    ;;       : can't figure out why atp->ref! crashes on scalars
    (let [assign-string (format "%s ← %s" var-name (str ops/jvm< apl-pointer))]
      (apl/run-simple-string! assign-string))
    (api/atp->ref! var-name apl-pointer)))

(defn jvm-type [^Pointer avp]
  ;; todo -- would be good to get rid of this reflection
  (if (h/scalar-pointer? avp)
        (clojure.core/type (ops/jvm< avp))
        tech.v3.tensor.Tensor))

