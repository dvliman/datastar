(ns datastar.main
  (:require
   [integrant.core :as ig]
   [integrant.repl :as repl]
   [datastar.http])
  (:gen-class))

(def config
  {:datastar.http/handler {}
   :datastar.http/server
   {:handler (ig/ref :datastar.http/handler)
    :port 5001
    :join? false}})

(defn -main []
  (repl/set-prep! (constantly config))
  (repl/go))
