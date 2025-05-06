(ns datastar.main
  (:require
   [selmer.filters]
   [selmer.parser]
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

(def base-url "http://localhost:5001")

(defn setup-selmer []
  (selmer.filters/add-filter!
   :base-url
   (fn [path]
     (if (.startsWith path "/")
       (str base-url path)
       (str base-url "/" path))))

  (selmer.parser/cache-off!))

(defn -main []
  (setup-selmer)
  (repl/set-prep! (constantly config))
  (repl/go))
