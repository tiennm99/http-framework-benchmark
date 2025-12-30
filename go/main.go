package main

import (
	"expvar"
	"fmt"
	"log"
	"net/http"
	_ "net/http/pprof"
	"time"

	"github.com/gin-gonic/gin"
)

type BenchInfo struct {
	InitValue float64 `form:"init"`
	LoopCount int     `form:"loop_count"`
	AddValue  float64 `form:"add"`
	MulValue  float64 `form:"mul"`
	SubValue  float64 `form:"sub"`
	DivValue  float64 `form:"div"`
	LineCount int     `form:"line"`
}

var (
	requestDuration    = expvar.NewInt("request_duration_ns")
	requestCount       = expvar.NewInt("request_count")
	requestAvgDuration = expvar.NewFloat("request_avg_duration_ns")
)

func main() {
	// Goroutine mới để chạy pprof, expvar
	go func() {
		log.Println("pprof:  http://localhost:6060/debug/pprof/")
		log.Println("expvar: http://localhost:6060/debug/vars")
		if err := http.ListenAndServe("localhost:6060", nil); err != nil {
			log.Printf("pprof error: %v", err)
		}
	}()

	// Create a Gin router with default middleware (logger and recovery)
	gin.SetMode(gin.ReleaseMode)

	r := gin.New()

	// Define a simple GET endpoint
	r.GET("/", func(c *gin.Context) {
		start := time.Now()
		var benchInfo BenchInfo

		// Bind query parameters to BenchInfo struct
		if err := c.ShouldBindQuery(&benchInfo); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		result := benchInfo.InitValue

		// Perform computations based on the provided parameters
		for range benchInfo.LoopCount {
			result += benchInfo.AddValue
			result *= benchInfo.MulValue
			result -= benchInfo.SubValue
			result /= benchInfo.DivValue
		}

		resultStr := fmt.Sprintf("result=%10f\n", result)

		payload := ""
		for range benchInfo.LineCount {
			payload += resultStr
		}

		// Send the computed payload as the response
		c.String(http.StatusOK, payload)

		requestCount.Add(1)
		requestDuration.Add(time.Since(start).Nanoseconds())
		requestAvgDuration.Set(float64(requestDuration.Value()) / float64(requestCount.Value()))
	})

	log.Println("server: http://localhost:8080")

	// Start server on port 8080
	if err := r.Run(); err != nil {
		log.Printf("r.Run() error: %v", err)
		return
	}
}
