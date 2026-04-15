package uno.dos.controllers.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import uno.dos.models.entity.Capacitacion;
import uno.dos.services.CapacitacionService;

@Controller
@RequestMapping("/calendario")
@RequiredArgsConstructor
public class CalendarioWebController {

    // 🔥 INYECCIÓN CORRECTA
    private final CapacitacionService capacitacionService;

    // 🔥 VISTA
    @GetMapping
    public String calendario(){
        return "calendario/index";
    }

    // 🔥 EVENTOS
    @GetMapping("/eventos")
    @ResponseBody
    public List<Map<String, Object>> eventos(){

        List<Capacitacion> lista = capacitacionService.listarActivas();

        List<Map<String, Object>> eventos = new ArrayList<>();

        for(Capacitacion c : lista){

            Map<String, Object> e = new HashMap<>();

            // 🔥 EVITAR NULL EN CURSO
            String curso = (c.getCurso() != null) 
                    ? c.getCurso().getNombreCurso() 
                    : "Sin curso";

            e.put("id", c.getId());
            e.put("title", curso + " - " + c.getNombreCapacitacion());

            // 🔥 EVITAR NULL EN FECHAS
            if(c.getFechaInicio() != null){
                e.put("start", c.getFechaInicio().toString());
            }

            if(c.getFechaFin() != null){
                e.put("end", c.getFechaFin().toString());
            }

            e.put("color", "#0ea5e9");
            e.put("allDay", true);

            eventos.add(e);
        }

        return eventos;
    }
}