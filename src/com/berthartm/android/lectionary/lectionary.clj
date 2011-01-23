(ns com.berthartm.android.lectionary.lectionary
    (:gen-class
     :extends android.app.Activity
     :exposes-methods {onCreate superOnCreate}))

(defn -onCreate [this #^android.os.Bundle bundle]
  (.superOnCreate this bundle)
  (.setContentView this com.berthartm.android.lectionary.R$layout/main))