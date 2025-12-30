package main

import (
	"bytes"
	"expvar"
	"fmt"
	"log"
	"net/http"
	_ "net/http/pprof"
	"time"

	"github.com/gofiber/fiber/v2"
)

type BenchInfo struct {
	InitValue float64 `query:"init"`
	LoopCount int     `query:"loop_count"`
	AddValue  float64 `query:"add"`
	MulValue  float64 `query:"mul"`
	SubValue  float64 `query:"sub"`
	DivValue  float64 `query:"div"`
	LineCount int     `query:"line"`
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

	// Create a Fiber app
	app := fiber.New(fiber.Config{
		DisableStartupMessage: false,
	})

	// Define a simple GET endpoint
	app.Get("/", func(c *fiber.Ctx) error {
		start := time.Now()
		var benchInfo BenchInfo

		// Bind query parameters to BenchInfo struct
		if err := c.QueryParser(&benchInfo); err != nil {
			return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{"error": err.Error()})
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

		payload := bytes.Repeat([]byte(resultStr), benchInfo.LineCount)

		// Send the computed payload as the response
		c.Set("Content-Type", "text/plain")
		requestCount.Add(1)
		requestDuration.Add(time.Since(start).Nanoseconds())
		requestAvgDuration.Set(float64(requestDuration.Value()) / float64(requestCount.Value()))

		return c.Send(payload)
	})

	log.Println("server: http://localhost:8080")

	// Start server on port 8080
	if err := app.Listen(":8080"); err != nil {
		log.Printf("app.Listen() error: %v", err)
		return
	}
}
