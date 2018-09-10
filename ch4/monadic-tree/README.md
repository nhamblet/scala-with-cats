## Monadic Tree

This roughly goes with the 4.10.1 exercise of the custom monad for the tree type.

Mostly it's to compare the for-comprehension version of tree modification from the
solutions with a hand-rolled one that doesn't depend on the monad and is relatively
[easy to write](src/main/scala/sandbox/Main.scala).

My take-away is that for the one-off of "convert leaves in a given specific way",
writing that by hand is easier than the `Monad` instance for `Tree` (especially the
`tailRecM`), but that probably if you were doing or allowing more modifications then
you'd grow to appreciate the for-comprehensions that `Monad` permits.

