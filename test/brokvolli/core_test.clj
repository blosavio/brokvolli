(ns brokvolli.core-test
  (:require [clojure.test :refer [are
                                  deftest
                                  is
                                  run-test
                                  run-tests
                                  testing]]
            [brokvolli.core :refer :all]))


(deftest concatv-tests
  (are [x y] (= x y)
    (concatv) []
    (concatv [] []) []
    (concatv [11 22 33]) [11 22 33]
    (concatv [11 22 33] []) [11 22 33]
    (concatv [] [11 22 33]) [11 22 33]
    (concatv [11 22 33] [44 55 66]) [11 22 33 44 55 66]
    (type (concatv [11 22 33] [44 55 66])) clojure.lang.PersistentVector))


#_(run-tests)

