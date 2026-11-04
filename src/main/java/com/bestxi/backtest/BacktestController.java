package com.bestxi.backtest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BacktestController {

    private final BacktestService backtestService;

    public BacktestController(BacktestService backtestService) {
        this.backtestService = backtestService;
    }

    // GET /admin/backtest?equipoId=543
    @GetMapping("/admin/backtest")
    public BacktestService.Resultado ejecutar(@RequestParam Long equipoId) {
        return backtestService.ejecutar(equipoId);
    }
}
