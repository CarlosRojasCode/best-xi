package com.bestxi.backtest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnProperty(name = "bestxi.admin-activo", havingValue = "true")
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
