(ns libapl-clj.prototype
  (:require [libapl-clj.api :as api]
            [libapl-clj.impl.ops :as ops]
            [libapl-clj.impl.pointer :as p]
            [libapl-clj.impl.helpers :as h]))

(defn jvm-fn [f]
  (fn
    ([] (f))
    ([arg & args]
     (let [args (into [arg]args)]
       (-> f
           (apply args)
           api/->jvm)))))

(defn monodyadic-fn'
  [fp]
  (fn
    ([] fp)
    ([arg] (api/fp+arg fp arg))
    ([arg1 arg2] (api/arg+fp+arg arg1 fp arg2))
    ([arg1 arg2 & more]
     (reduce #(api/arg+fp+arg (api/value->apl %1) fp (api/value->apl %2))
             (into [arg1 arg2] more)))))

(def monodyadic-fn (jvm-fn monodyadic-fn))

(defn left-operator-fn'
  [op]
  (fn
    ([] op)
    ([fp arg]
     (api/fp+op+arg (fp) op arg))
    ([fp axis arg]
     (api/fp+op+axis+arg (fp) op axis arg))))

(def left-operator-fn (jvm-fn left-operator-fn'))

(defn dyadic-operator-fn'
  [op]
  (fn
    ([] op)
    ([fp1 fp2 arg]
     (api/fp+op+fp+arg (fp1) op (fp2) arg))
    ([fp1 fp2 axis arg]
     (api/fp+op+fp+axis+arg (fp1) op (fp2) axis))))

(def dyadic-operator-fn (jvm-fn dyadic-operator-fn))


#_(defn monadic-operator-fn
  [fp]
  (fn
    ([] fp)
    ([arg]
     (api/fp+arg fp arg))
    ([])))
