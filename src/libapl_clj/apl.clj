(ns libapl-clj.apl
  (:refer-clojure :exclude [+ +' - -' = *' * max min < <= > >= not=
                            concat reduce format take drop reductions reverse
                            range identical? find])
  (:require [libapl-clj.api :as api :reload true]
            [libapl-clj.impl.pointer :as p :reload true]
            [libapl-clj.prototype :as proto]
            [tech.v3.tensor :as tensor]
            [libapl-clj.impl.helpers :as h]
            [libapl-clj.impl.ops :as ops]))

(defn initialize!
  "Initialize the APL shared library.  Currently only tested on Linux."
  []
  (api/initialize!)
  :ok)

(defn run-simple-string!
  "Run an APL command. Returns `true` if successful."
  [cmd]
  (zero? (api/run-simple-string! cmd)))

(def ->apl api/->apl)

(def ->jvm api/->jvm)

(def ->string api/->string)

(defn display! [x]
  (h/drawing! (->apl x)))

(def +' (proto/monodyadic-fn' p/+))
(def + (proto/jvm-fn +'))

(def -' (proto/monodyadic-fn' p/-))
(def - (proto/jvm-fn -'))

(def ×' (proto/monodyadic-fn' p/×))
(def × (proto/jvm-fn ×'))
(def mul ×)

(def =' (proto/monodyadic-fn' p/=))
(def = (proto/jvm-fn ='))
(def mul =)

(def ÷' (proto/monodyadic-fn' p/÷))
(def ÷ (proto/jvm-fn ÷))
(def div ÷)

(def *' (proto/monodyadic-fn' p/*))
(def * (proto/jvm-fn *'))
(def pow *)

(def ⌈' (proto/monodyadic-fn' p/⌈))
(def ⌈ (proto/jvm-fn ⌈'))
(def max ⌈)

(def ⌊' (proto/monodyadic-fn' p/⌊))
(def ⌊ (proto/jvm-fn ⌊'))
(def min ⌊)

(def |' (proto/monodyadic-fn' p/|))
(def | (proto/jvm-fn |'))
(def residue |)

(def <' (proto/monodyadic-fn' p/<))
(def < (proto/jvm-fn <'))

(def ≤' (proto/monodyadic-fn' p/≤))
(def ≤ (proto/jvm-fn ≤))
(def <= ≤)

(def =' (proto/monodyadic-fn' p/=))
(def = (proto/jvm-fn ='))

(def >' (proto/monodyadic-fn' p/>))
(def > (proto/jvm-fn >'))

(def ≥' (proto/monodyadic-fn' p/≥))
(def ≥ (proto/jvm-fn ≥'))
(def >= ≥)


(def ≠' (proto/monodyadic-fn' p/≠))
(def ≠ (proto/jvm-fn ≠'))
(def not= ≠)

(def ⍴' (proto/monodyadic-fn' p/⍴))
(def ⍴ (proto/jvm-fn ⍴'))

(def ∊' (proto/monodyadic-fn' p/∊))
(def ∊ (proto/jvm-fn ∊'))

(def ∨' (proto/jvm-fn p/∨))
(def ∨ (proto/jvm-fn p/∨))
(def b-or ∨)

(def ∧' (proto/monodyadic-fn' p/∧))
(def ∧ (proto/jvm-fn ∧'))
(def b-and ∧)

(def ⍱' (proto/monodyadic-fn' p/⍱))
(def ⍱ (proto/jvm-fn ⍱))
(def b-nor ⍱)

(def ⍲' (proto/monodyadic-fn' p/⍲))
(def ⍲ (proto/jvm-fn ⍲'))
(def b-nand ⍲)

(def ravel' (fn
              ([] p/...)
              ([arg] (api/fp+arg p/... arg))
              ([axis arg] (api/fp+axis+arg p/... axis arg))))
(def ravel (proto/jvm-fn ravel'))

(def concat' (fn concat'
               ([] p/...)
               ([arg1 arg2]
                (api/arg+fp+arg arg1 p/... arg2))
               ([axis arg1 arg2]
                (api/arg+fp+axis+arg arg1 p/... axis arg2))
               ([axis arg1 arg2 & more]
                (let [args (into [arg1 arg2] more)]
                  (clojure.core/reduce
                   (fn [a b] (concat' axis a b))
                   args)))))
(def concat (proto/jvm-fn concat'))

(def laminate' (fn
                 ([] p/...)
                 ([axis arg1 arg2]
                  (api/arg+fp+axis+arg arg1 p/... axis arg2))))
(def laminate (proto/jvm-fn laminate'))

(def ⍪' (proto/monodyadic-fn' p/⍪))
(def ⍪ (proto/jvm-fn ⍪))

(def compress' (proto/monodyadic-fn' p//))
(def compress (proto/jvm-fn compress))

(def reduce' (proto/left-operator-fn' p//))
(def reduce (proto/jvm-fn reduce'))

(def ⌿' (proto/left-operator-fn' p/⌿))
(def ⌿ (proto/jvm-fn ⌿'))
(def reduce-last ⌿)

(def ?' (proto/monodyadic-fn' p/?))
(def ? (proto/jvm-fn ?'))
(def roll' (fn roll'
            ([] ?')
            ([arg] (?' arg))))
(def roll (proto/jvm-fn roll'))
(def deal' roll')
(def deal roll)

(def ⍕' (proto/monodyadic-fn' p/⍕))
(def ⍕ (proto/jvm-fn ⍕'))
(def format ⍕)

(def ⍎' (proto/monodyadic-fn' p/⍎))
(def ⍎ (proto/jvm-fn ⍎'))
(def execute ⍎)

(def ∼' (proto/monodyadic-fn' p/∼))
(def ∼ (proto/jvm-fn ∼'))
(def invert ∼)

(def ↑' (proto/monodyadic-fn' p/↑))
(def ↑ (proto/jvm-fn ↑'))
(def take ↑)

(def ↓' (proto/monodyadic-fn' p/↓))
(def ↓ (proto/jvm-fn ↓'))

(def reductions' (proto/left-operator-fn' p/backslash))
(def reductions (proto/jvm-fn reductions'))
(def scan' reductions')
(def scan reductions)

(def ⌽' (proto/monodyadic-fn' p/⌽))
(def ⌽ (proto/jvm-fn ⌽))
(def reverse-last ⌽)


(def ⊖' (proto/monodyadic-fn' p/⊖))
(def ⊖ (proto/jvm-fn ⊖'))
(def reverse ⊖)

(def ⍉' (proto/monodyadic-fn' p/⍉))
(def ⍉ (proto/jvm-fn ⍉'))
(def transpose ⍉)

(def ¯' (fn ¯'
          ([] p/¯)
          ([arg]
           (api/fp+arg p/- arg))))
(def ¯ (proto/jvm-fn ¯'))

(def ⍬' p/⍬)
(def ⍬ nil)
(def zilde' ⍬')
(def zilde nil)

(def ⍋' (proto/monodyadic-fn' p/⍋))
(def ⍋ (proto/jvm-fn ⍋))
(def grade-up ⍋)

(def ⍒' (proto/monodyadic-fn' p/⍒))
(def ⍒ (proto/jvm-fn ⍒'))
(def grade-down ⍒)

(def ⊥' (proto/monodyadic-fn' p/⊥))
(def ⊥ (proto/jvm-fn ⊥))
(def decode ⊥)

(def ⊤' (proto/monodyadic-fn' p/⊤))
(def ⊤ (proto/jvm-fn ⊤))
(def encode ⊤)

(def ⍟' (proto/monodyadic-fn' p/⍟))
(def ⍟ (proto/jvm-fn ⍟'))
(def ln ⍟)

(def !' (proto/monodyadic-fn' p/!))
(def ! (proto/jvm-fn !'))
(def factorial !)

(def ○' (proto/monodyadic-fn' p/○))
(def ○ (proto/jvm-fn ○'))
(def pi* ○)

(def ⌹' (proto/monodyadic-fn' p/⌹))
(def ⌹ (proto/jvm-fn ⌹'))
(def matrix-invert ⌹)

(def ⍳' (proto/monodyadic-fn' p/⍳))
(def ⍳ (proto/jvm-fn ⍳'))
(def range ⍳)

(def ≡' (proto/monodyadic-fn' p/≡))
(def ≡ (proto/jvm-fn ≡'))
(def identical? ≡)

(def ≢' (proto/monodyadic-fn' p/≢))
(def ≢ (proto/jvm-fn ≢'))
(def not-identical? ≢)

#_(;; currently broken
   (def ¨' (proto/left-operator-fn' p/¨))
   (def ¨ (proto/jvm-fn ¨')))

(def ⍷' (proto/monodyadic-fn' p/⍷))
(def ⍷ (proto/jvm-fn ⍷'))
(def find ⍷)

(def ⊂' (proto/monodyadic-fn' p/⊂))
(def ⊂ (proto/jvm-fn ⊂'))

(def ⊃'(proto/monodyadic-fn' p/⊃))
(def ⊃ (proto/jvm-fn ⊃'))

(def inner-product' (fn inner-product'
                      [left right arg1 arg2]
                      (api/arg+fp+op+fp+arg
                       arg1 (left) p/. (right) arg2)))
(def inner-product (proto/jvm-fn inner-product'))


(comment

  (+ (->apl (->apl [1 2])) [1 2])

  (- (libapl-clj.impl.api/int-scalar 2)
     (tensor/->tensor [1 2])
     (->apl [3 4])
     5)

  (display! ((comp -' +')
             [1 2] [3 4] [5 6])) 
  
  (display! (+ [[1 2]
                [3 4]]

               [[3 4]
                [5 6]]

               [[5 6]
                [7 8]]))

  (display! (- [[1 2]
                [3 4]]

               [[5 6]
                [7 8]]))

  (display! (⍴' [2 3 5]
             (+' [1 2] [3 4])))


  (display! (= " " "hello there"))

  (display! (∊ "abcde" "why is this a word"))

  (display!
   (⍴ [2 3 5]
    (+ [1 2] [3 4])))
  
  (⌈
   [[3 2]
    [5 6]]
   
   [[1 2]
    [2 4]]
   
   [[1 5]
    [8 9]])

  (reduce ⌈ 0  [[1 2 3 4]
                [1 2 3 4]])

  (↑ 1 [1 2 3])

  (↓ 1 [1 2 3])

  (laminate [0.5] [1 2 3] \a)
  (laminate [-0.5] [1 2 3] \a)

  (display! (-> (concat 0 [[1 2]] [[4 5]] [[5 6]] "ab")
                (concat  \c)))
    
  (display! (concat 1 [[1 2]] [[4 5]]))


  (initialize!)
  (run-simple-string! "res ← 4 4 ⍴ 3")
  (def res (value-pointer "res"))

  (long res)
  (vec (.getMethods (.getClass res)))
  (.getLong res)
  (long res)
  (java.beans.BeanInfo. res)
  (res )
  res
  (pointer->string res)
  (pointer->rank res)
  (pointer->count res)
  (jna/get_value res 0)

  (jna/get_type res 1)

  (tensor/nd-buffer-descriptor->tensor)

  (tech.v3.datatype/as-nd-buffer-descriptor
   res
   )
  


  
  (run-simple-string! "x ← 1.0 1.1 'a' 'hello' 'hey' 'b'")
  (def x (value-pointer "x"))
  x
  (pointer->string x)
  (= (jna/get_type x 0) 0x10 16) ;; integer
  (= (jna/get_type x 1) 0x20 32) ;; float
  (= (jna/get_type x 2) 0x02  2) ;; character
  (= (jna/get_type x 3) 0x04  4) ;; char point / string
  
  (run-simple-string! "x1 ← ⍳3")
  (def x1 (value-pointer "x1"))
  (jna/get_type x1 0)
  (jna/get_real x1 0)

  
  (run-simple-string! "res1 ← res + res")
  (def res1 (value-pointer "res1"))
  res
  (pointer->string res1)
  (pointer->rank res1)
  (pointer->count res1))
