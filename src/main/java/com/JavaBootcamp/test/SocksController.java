package com.JavaBootcamp.test;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
class SocksController {

    private final SocksModelAssembler assembler;
    private final SocksRepository repository;


    SocksController(SocksRepository repository, SocksModelAssembler assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }


    @GetMapping("/socks")
    CollectionModel<EntityModel<Socks>> all() {

        List<EntityModel<Socks>> socks = repository.findAll().stream() //
                .map(assembler::toModel) //
                .collect(Collectors.toList());

        return CollectionModel.of(socks, linkTo(methodOn(SocksController.class).all()).withSelfRel());
    }

    @PostMapping("/api/socks/income")
    ResponseEntity<?> newSocks(@RequestBody Socks newSocks) {
         Socks s = new Socks();
         // условие для поиска соответствий по цвету и содержанию хлопка.
        if (repository.findByCottonPartAndColor(newSocks.getCottonPart(), newSocks.getColor()) != null){
        Socks updateSocks = repository.findById(s.getId())//Работает так как надо при прямой привязке к Id
                .map(socks -> {                             //@PathVariable Long id в конструктор
                    socks.setQuantity(socks.getQuantity() + newSocks.getQuantity());
                    return repository.save(socks);
                })
                .orElseGet(() -> {
                    newSocks.setId(s.getId());
                    return repository.save(newSocks);
                });
        EntityModel<Socks> entityModel = assembler.toModel(updateSocks);

        return ResponseEntity //
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .body(entityModel);
        }
        else { EntityModel<Socks> entityModel = assembler.toModel(repository.save(newSocks));

            return  ResponseEntity //
                    .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                    .body(entityModel);}
    }



    // Single item


    @GetMapping("/socks/{id}")
    EntityModel<Socks> one(@PathVariable Long id) {

        Socks socks = repository.findById(id) //
                .orElseThrow(() -> new SocksNotFoundException(id));

        return assembler.toModel(socks);
    }

    @PutMapping("/socks/{id}")
    ResponseEntity<?> replaceSocks(@RequestBody Socks newSocks, @PathVariable Long id) {
        Socks updateSocks = repository.findById(id)
                .map(socks -> {
                    socks.setQuantity(newSocks.getQuantity());
                    socks.setCottonPart(newSocks.getCottonPart());
                    socks.setColor(newSocks.getColor());
                    return repository.save(socks);
                })
                .orElseGet(() -> {
                    newSocks.setId(id);
                    return repository.save(newSocks);
                });
        EntityModel<Socks> entityModel = assembler.toModel(updateSocks);

        return ResponseEntity //
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .body(entityModel);
    }

    @DeleteMapping("/socks/{id}")
    ResponseEntity<?> deleteEmployee(@PathVariable Long id) {

        repository.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
