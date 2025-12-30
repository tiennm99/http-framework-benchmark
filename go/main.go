package main

import (
	"fmt"
  	"net/http"
  	"github.com/gin-gonic/gin"
)

type BenchInfo struct {
  InitValue     float64    `form:"init"`
  LoopCount  	int    		`form:"loop_count"`
  AddValue    	float64    `form:"add"`
  MulValue    	float64    `form:"mul"`
  SubValue    	float64    `form:"sub"`
  DivValue    	float64    `form:"div"`
  LineCount   	int    		`form:"line"`
}

func main() {
	// Create a Gin router with default middleware (logger and recovery)
	gin.SetMode(gin.ReleaseMode)

	r := gin.New()

	// Define a simple GET endpoint
	r.GET("/", func(c *gin.Context) {

		var benchInfo BenchInfo
		
		// Bind query parameters to BenchInfo struct
		err := c.ShouldBindQuery(&benchInfo)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		var result float64 = benchInfo.InitValue
		
		// Perform computations based on the provided parameters
		for i := 0; i < benchInfo.LoopCount; i++ {
			result += benchInfo.AddValue
			result *= benchInfo.MulValue
			result -= benchInfo.SubValue
			result /= benchInfo.DivValue
		}

		var resultStr string = fmt.Sprintf("result=%10f\n", result)

		var payload string = ""
		for i := 0; i < benchInfo.LineCount; i++ {
			payload += resultStr
		}

		// Send the computed payload as the response
		c.String(http.StatusOK, payload)
	})

	// Start server on port 8080 (default)
	// Server will listen on 0.0.0.0:8080 (localhost:8080 on Windows)
	r.Run()
}