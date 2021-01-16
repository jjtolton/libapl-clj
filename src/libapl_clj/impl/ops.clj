(ns libapl-clj.impl.ops
  (:require tech.v3.datatype.jna
            [tech.v3.datatype :as dtype]
            [tech.v3.tensor :as tensor]
            [libapl-clj.impl.jna :as jna :reload true]
            [libapl-clj.impl.api :as api]
            [libapl-clj.impl.pointer :as p]
            libapl-clj.impl.protocols
            [complex.core :refer [complex]]
            [libapl-clj.impl.helpers :as h :reload true])
  (:import [java.util UUID]))


(defn run-simple-string!
  "Run an APL command. Returns `true` if successful."
  [cmd]
  (zero? (api/run-simple-string! cmd)))


(defn initialize! []
  (api/initialize!)
  (api/run-simple-string! "⎕io ← 0")
  :ok)


(defonce _ (initialize!))


(defn ->scalar-pointer [x]
  ;; todo -- reflection, gross.  This could benefit from
  ;; a protocol and type extension
  (cond
    (int? x)                    (jna/int_scalar x "")
    (or (float? x) (double? x)) (jna/double_scalar (double x) "")
    (char? x)                   (jna/char_scalar x "")
    (string? x)                 (jna/char_vector x "")
    :else                       (throw (ex-info "Invalid type"
                                                {:type (type x)}))))

(defn ->shape [p-or-t]
  ;; needs to have reflection removed
  (if-let [t (and (tensor/tensor? p-or-t)
                  p-or-t)]
    (dtype/shape t)
    (let [p    p-or-t
          rank (jna/get_rank p)]
      (->> (range rank)
           (mapv #(jna/get_axis p %))
           (tensor/->tensor)))))


(comment
  (PTensor. (api/char-vector "hello")))

(declare ->tensor) ;; ->tensor is mutually recursive with jvm<

(defn jvm< [apl-value idx]
    (case (api/type-at-idx apl-value idx)
      0    nil

      ;; char
      0x02 (api/char< apl-value idx)

      ;; pointer, probably a vector or char-vector
      0x04 (->> idx
                (api/avp< apl-value)
                ->tensor)
    
      ;; todo CELLREF -- never encountered this, not sure what it is
      0x08 (->> idx
                (api/avp< apl-value)
                ->tensor)

      ;; int
      0x10 (api/int< apl-value idx)

      ;; float, double, or real part of complex number
      0x20 (api/real< apl-value idx)

      ;; complex number
      0x40 (complex (api/real< apl-value idx)
                    (api/imag< apl-value idx))))

(defn ->tensor [apl-value]
  (let [rank        (api/rank apl-value)
        n           (api/n-elems apl-value)
        shape'      (->shape apl-value)
        random-name (h/random-var-name)]
    (-> (dtype/make-reader :object n (jvm< apl-value idx))
        (tensor/construct-tensor
         (tech.v3.tensor.dimensions/dimensions
          (dtype/->reader shape'))))))

(defn set-scalar! [atp idx elt]
  ;; todo -- remove reflection
  (cond
    (int? elt)  (api/set-int! atp idx elt)
    (or (double? elt)
        (float? elt))   (api/set-double! atp idx elt)
    (char? elt) (api/set-char! atp idx elt)))

(declare tensor->apl) ;; set-compound! is mutually recursive with tensor->apl

(defn set-compound! [atp idx elt]
  ;; todo -- remove reflection
  (let [val (if (string? elt)
              (api/char-vector elt)
              (-> elt
                  seq
                  dtype/ensure-reader
                  tensor/ensure-tensor
                  tensor->apl))]
    (api/set-value! atp idx val)))

(defn tensor->apl [tensor]
  ;; todo -- gc
  (let [elems (dtype/->reader tensor)
        shape (dtype/shape tensor)
        atp   (api/apl-value (-> shape dtype/->reader vec))]
    (doseq [[idx elt] (->>  tensor
                            tensor/tensor->buffer
                            (interleave (range))
                            (partition 2))]
      (tap> {:tensor->apl/atp atp})
      (tap> {:tensor->apl/idx idx})
      (tap> {:tensor->apl/elt elt})
      ;; todo -- still a lot of reflection that needs to be removed
      (if (h/scalar? elt)
        (set-scalar! atp idx elt)
        (set-compound! atp idx elt)))
    atp))

(comment
  (add-tap (fn [x]
             (when-let [elt (and (map? x)
                                 (:tensor->apl/elt x))]
               (def elt' elt))))
  (add-tap (fn [x]
             (when-let [idx (and (map? x)
                                 (:tensor->apl/idx x))]
               (def idx' idx))))
  (add-tap (fn [x]
             (when-let [atp (and (map? x)
                                 (:tensor->apl/atp x))]
               (def atp' atp)))))

(comment
  (def tensor (tensor/->tensor (range 3)))
  (-> (tensor/->tensor (range 3))
      tensor->apl
      drawing!)
  (-> (tensor/->tensor [\d]) tensor->apl drawing!)
  (-> (tensor/->tensor [1 2 3 "hello" [1.12 (double 2.4) (long 3.15) [2 3 4 5]]
                        (tensor/->tensor [1 2 3])])
      tensor->apl
      ->tensor
      tensor->apl
      drawing!)
  
  (drawing! (api/apl-value [5 2 3])))



