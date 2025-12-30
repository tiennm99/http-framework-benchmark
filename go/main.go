package main

import (
	"bytes"
	"encoding/json"
	"expvar"
	"fmt"
	"log"
	"net/http"
	_ "net/http/pprof"
	"strconv"
	"time"
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

	// Create a new serve mux
	mux := http.NewServeMux()

	// Define a simple GET endpoint
	mux.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		start := time.Now()
		var benchInfo BenchInfo

		// Parse query parameters manually
		if err := parseQueryParams(r, &benchInfo); err != nil {
			w.WriteHeader(http.StatusBadRequest)
			json.NewEncoder(w).Encode(map[string]string{"error": err.Error()})
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

		payload := bytes.Repeat([]byte(resultStr), benchInfo.LineCount)

		// Send the computed payload as the response
		w.Header().Set("Content-Type", "text/plain")
		w.WriteHeader(http.StatusOK)
		w.Write(payload)

		requestCount.Add(1)
		requestDuration.Add(time.Since(start).Nanoseconds())
		requestAvgDuration.Set(float64(requestDuration.Value()) / float64(requestCount.Value()))
	})

	log.Println("server: http://localhost:8080")

	// Start server on port 8080
	if err := http.ListenAndServe(":8080", mux); err != nil {
		log.Printf("ListenAndServe error: %v", err)
		return
	}
}

// parseQueryParams manually parses query parameters into BenchInfo struct
func parseQueryParams(r *http.Request, info *BenchInfo) error {
	query := r.URL.Query()

	if val := query.Get("init"); val != "" {
		f, err := strconv.ParseFloat(val, 64)
		if err != nil {
			return err
		}
		info.InitValue = f
	}

	if val := query.Get("loop_count"); val != "" {
		i, err := strconv.Atoi(val)
		if err != nil {
			return err
		}
		info.LoopCount = i
	}

	if val := query.Get("add"); val != "" {
		f, err := strconv.ParseFloat(val, 64)
		if err != nil {
			return err
		}
		info.AddValue = f
	}

	if val := query.Get("mul"); val != "" {
		f, err := strconv.ParseFloat(val, 64)
		if err != nil {
			return err
		}
		info.MulValue = f
	}

	if val := query.Get("sub"); val != "" {
		f, err := strconv.ParseFloat(val, 64)
		if err != nil {
			return err
		}
		info.SubValue = f
	}

	if val := query.Get("div"); val != "" {
		f, err := strconv.ParseFloat(val, 64)
		if err != nil {
			return err
		}
		info.DivValue = f
	}

	if val := query.Get("line"); val != "" {
		i, err := strconv.Atoi(val)
		if err != nil {
			return err
		}
		info.LineCount = i
	}

	return nil
}
