## Oracle R Enterprise 1.3
## ORE Ensemble Model Example
## (c)2013 Oracle - All Rights Reserved
##
#####################################################

##
# bootstrapSample
##
bootstrapSample <- function (data, num.samples, size) {
  num.rows <- nrow(data)
  sample.indexes <- lapply(1:num.samples, function(x) sample(num.rows, size, replace=TRUE))
  
  create.sample <- function (sample.indexes, data) {
    ore.push (data[sample.indexes,]) 
  }
  
  samples <- lapply(sample.indexes, create.sample, data)
  samples 
}

##
# bagReg
##
bagReg <- function (data, formula, num.models, sample.size) {
  samples <- bootstrapSample (data, num.samples=num.models, sample.size)
  
  build.ore.lm <- function (data, formula, ...){
    ore.lm(formula, data, ...)
  }
  
  models <- lapply(samples, build.ore.lm, formula)
  models
}

##
# bagReg2 - parallel option
##
bagReg2 <- function (data.name, formula, num.models, sample.size, parallel=FALSE) {
  
  models <- ore.indexApply (num.models,
                              function(index, data.name, formula, sample.size){
                              library(ORE)                              
                              ore.connect("moviedemo", "orcl", "localhost", "welcome1")
			      ore.sync()
                              set.seed(index)
                              data <- ore.get(data.name)
                              num.rows <- nrow(data)
                              sample.indexes <- sample(num.rows, sample.size, replace=TRUE)
                              row.names(data) <- data$ID
                              data <- data[sample.indexes,]
                              ore.lm(formula, data)
                            },
                            data.name, formula, sample.size, 
                            parallel=parallel )
  ore.pull(models)
}

##
# predict.bagReg
##
predict.bagReg <- function (models, data, supp.cols) {
  
  score.ore.lm <- function (model, data, supp.cols) {
    res <- data.frame(data[,supp.cols])
    res$PRED <- predict (model, data)
    res
  }
  
  predictions <- lapply (models, score.ore.lm, data, supp.cols)
  scores <- predictions[[1L]][,c(supp.cols)]
  predValues <- lapply(predictions, function(y) y[, "PRED"])
  scores$PRED_MIN  <- do.call(pmin, predValues)
  scores$PRED <- rowMeans(do.call(ore.frame, predValues))
  scores$PRED_MAX  <- do.call(pmax, predValues)
  scores
}

##
# ore.rmse
##
ore.rmse <- function (pred, obs) {
  sqrt(mean(pred-obs)^2)
}



