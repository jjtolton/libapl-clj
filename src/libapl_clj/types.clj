(ns libapl-clj.types
  (:require [complex.core :refer [complex]]))
  


org.apache.commons.math3.complex.Complex

(def type-map {0    nil
               0x02 ::char
               0x04 ::tensor
               0x08 ::tensor
               0x10 ::integer
               0x20 ::double
               0x40 ::complex})

(def type->key-type {nil                                      nil
                     Character                                ::char
                     tech.v3.Tensor                           ::tensor
                     Integer                                  ::integer
                     Double                                   ::double
                     org.apache.commons.math3.complex.Complex ::complex})
