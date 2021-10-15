(ns user)

(def scripts-path (.getScriptsPath api))
(def boostrap-path (str scripts-path "/bootstrap.clj"))
(println "Loading bootstrap from" boostrap-path)
(load-file boostrap-path)
