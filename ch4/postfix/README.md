This is a base project, using the default suggested new project setup, to work through
exercise 4.9 on post-order calculators.

I've left some of my failed attempts in as breadcrumbs, and the indentation is poor, sorry.

### Why Bother?

I've actually got two versions of a solution to the exercise. One uses `State` as suggested,
the other does not, to compare why I'd care to use the monad. The major difference in the
actual lines of code seems to be that in the non-`State`-based solution, you manually
pass the state around between the various helper functions, so it can make its way down to
where the work will happen.

Is passing the state down into each function bad? Ugly? More or less intuitive? Fewer parameters
in a function is a good thing. While the signatures all intuitively have the form "take a `List[Int]`
(and maybe more) and return a `List[Int]`", there's no explicit suggestion that the two are
related. This isn't entirely different from the `State`-based version, besides that there you
know you're dealing with a state transformation.

One question is if either version better supports some reasonable modification you might want
to make. Both would be about the same amount of work to add another operator, or change from
ints to floats.

Since `State` is separating the state modification from the final answer, I think in the
`State`-based version it is easier to change or extend the thing you are calculating. Suppose
I wanted to know, for a program, not just what the final answer was, but also how many operators
were used, what the largest value seen was, how many unique values were seen, etc. I believe,
in this case, the `State`-based solution will be much easier to modify, because the channels
for returning more information are already around.

