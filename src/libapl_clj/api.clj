(ns ^{:doc "A (more) stable API for working in APL memory space.  Coercion
from JVM->APL provided. This is the suggested API for library development."}
    libapl-clj.api
  (:require [libapl-clj.impl.api :as api]
            [libapl-clj.impl.ops :as ops]
            [libapl-clj.impl.helpers :as h]
            [libapl-clj.apl :as apl]
            [libapl-clj.types :as apl-types]
            [tech.v3.tensor :as tensor]
            [tech.v3.datatype :as dtype])
  (:import [com.sun.jna Pointer]))

(def initialize! ops/initialize!)

(def run-simple-string! ops/run-simple-string!)

(def run-command-string! api/run-command-string!)

(def ->string api/pointer->string)

(def rank api/rank)

(def axis api/axis)

(def n-elems api/n-elems)

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
    (-> avp
        ops/jvm<
        clojure.core/type
        apl-types/type->key-type)
    ::apl-types/tensor))

(defn jvm-type-at-idx [^Pointer apl-value ^Integer idx]
  (-> (api/type-at-idx apl-value idx)
      (get apl-types/type-map)))

(defn vget
  ([^Pointer apl-value ^Integer idx]
   (vget apl-value idx nil))
  ([^Pointer apl-value ^Integer idx not-found]
   (when-not (h/scalar-pointer? apl-value)
     (let [e-count (n-elems apl-value)]
       (if (< idx e-count)
         (if (= ::apl-types/tensor (jvm-type-at-idx apl-value idx))
           (api/atp+idx->avp apl-value idx)
           (ops/jvm< apl-value idx))
         not-found)))))

#_(defn vget-in [^Pointer apl-value path]
  (when-not (h/scalar-pointer? apl-value)
    (when-let [[coordinate & path] (not-empty path)]
      (let [e-count (n-elems apl-value)])
      )))

(def set-char! api/set-char!)

(def set-int! api/set-int!)

(def set-value! api/set-value!)

(def set-double! api/set-double!)

(def set-complex! api/set-complex!)

(defn ndarray [shape]
  (api/apl-value shape))

(defn new-scalar []
  (api/apl-scalar))

(defn new-vector [length]
  (api/apl-vector length))

(defn new-cube [blocks rows cols]
  (api/apl-cube blocks rows cols))

(defn apl-string [s]
  (api/char-vector s))

(defn apl-int [n]
  (api/int-scalar n))

(defn apl-double [n]
  (api/double-scalar n))

(defn apl-complex [real imag]
  (api/complex-scalar real imag))


(defn ->apl [x]
  ;; need to fix reflection
  (cond
    (h/scalar? x) (ops/->scalar-pointer x)
    (string? x)   (api/char-vector x)
    (seqable? x)  (-> x tensor/->tensor ops/tensor->apl)
    :else         (throw (ex-info "Unsupported type"
                                  {:value x
                                   :type  (type x)}))))

(defn value->apl [v]
  {:pre [#(or (h/scalar? v)
              (string? v)
              (vector? v)
              (list? v)
              (seq? v))]}
  (->apl v))



(defn arg+fp+arg ^Pointer [arg1 fp arg2]
  (api/arg+fp+arg (value->apl arg1) fp (value->apl arg2)))

(defn arg+fp+axis+arg ^Pointer [arg1 fp axis arg2]
  (api/arg+fp+axis+arg (value->apl arg1)
                       fp
                       (value->apl axis)
                       (value->apl arg2)))

(defn arg+fn+op+axis+arg ^Pointer [arg1 fp op axis arg2]
  (api/arg+fn+op+axis+arg (value->apl arg1)
                          fp
                          op
                          (value->apl axis)
                          (value->apl arg2)))

(defn arg+fn+op+fn+axis+arg ^Pointer
  [arg1 fp1 op fp2 axis arg2]
  (api/arg+fn+op+fn+axis+arg
   (value->apl arg1)
   fp1
   op
   fp2
   (value->apl axis)
   (value->apl arg2)))

(defn fp+arg ^Pointer [^Pointer fp arg]
  (api/fp+arg fp (value->apl arg)))

(defn fp+op+arg ^Pointer [^Pointer fp ^Pointer op arg]
  (api/fp+op+arg fp op (value->apl arg)))

(defn fp+op+axis+arg ^Pointer [^Pointer fp ^Pointer op ^Pointer axis arg]
  (api/fp+op+axis+arg fp op (value->apl axis) (value->apl arg)))

(defn fp+op+axis+arg ^Pointer [^Pointer fp ^Pointer op ^Pointer axis arg]
  (api/fp+op+axis+arg fp op (value->apl axis) (value->apl arg)))

(defn fp+op+fp+axis+arg ^Pointer [^Pointer fp1 ^Pointer op ^Pointer fp2 ^Pointer axis arg]
  (api/fp+op+fp+axis+arg fp1 op fp2 (value->apl axis) (value->apl arg)))


(comment
  (require '[libapl-clj.impl.pointer :as p])
  (h/drawing! (arg+fp+arg 1 p/+ 1)) ;; 1 + 1
  (h/drawing! (fp+arg p/⍳ 10))  ;; ⍳10
  (h/drawing! (arg+fp+arg [1 2 3] p/+ [1 2 3])) ;; 1 2 3 + 1 2 3
  (h/drawing! (arg+fp+arg [1 2 3] p/+ 10)) ;; 1 2 3 + 10
  (h/drawing! (arg+fp+arg 10 p/+ [1 2 3]))) ;; 10 + 1 2 3
