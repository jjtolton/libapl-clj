(ns apl-test
  (:require  [clojure.test :as t :refer [deftest testing is]]
             [libapl-clj.apl :as apl]
             [tech.v3.datatype :as dtype]
             [tech.v3.tensor :as tensor]))

(deftest test->apl
  (testing "testing ->apl"
    (let [p (apl/->apl [1 2])]
      (is (= [1 2] (-> p apl/->jvm dtype/->reader vec)))
      (is (= [1 2] (-> p
                       apl/->apl
                       apl/->jvm
                       apl/->apl
                       apl/->jvm
                       dtype/->reader
                       vec)))))
  (testing "nested ->apl"
    (let [structure [1 2
                     "nested"
                     ["data"
                      [2 3 4
                       ["structure"
                        5 6 7]]]
                     ["is" ["here"]]]
          p         (apl/->apl structure)
          ravel     (-> p
                        apl/->jvm
                        dtype/->reader
                        vec
                        flatten)]
      (is (= ravel
             [1 2
              \n \e \s \t \e \d
              \d \a \t \a
              2 3 4
              \s \t \r \u \c \t \u \r \e
              5 6 7
              \i \s
              \h \e \r \e])))))













