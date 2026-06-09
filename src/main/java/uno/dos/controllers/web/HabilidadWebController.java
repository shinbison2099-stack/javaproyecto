package uno.dos.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import uno.dos.services.HabilidadService;
import uno.dos.services.SubAreaService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/habilidad")
public class HabilidadWebController {

    private final HabilidadService habilidadService;
    private final SubAreaService subAreaService;

}