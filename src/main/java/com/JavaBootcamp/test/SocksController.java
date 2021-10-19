package com.JavaBootcamp.test;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
@RestController
class SocksController {
    @Autowired

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
        Socks socksForSave;
        List<Socks> filteredSocksList = repository.findByCottonPartAndColor(newSocks.getCottonPart(), newSocks.getColor());

        if (filteredSocksList.size() > 0) {
            Socks foundSocks = filteredSocksList.get(0);
            socksForSave = repository.getById(foundSocks.getId());
            socksForSave.setQuantity(foundSocks.getQuantity() + newSocks.getQuantity());
        } else
            socksForSave = newSocks;

        repository.save(socksForSave);
        EntityModel<Socks> entityModel = assembler.toModel(socksForSave);

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PostMapping("/api/socks/outcome")
    ResponseEntity<?> outcomeSocks(@RequestBody Socks newSocks) {
        Socks socksForSave;
        List<Socks> filteredSocksList = repository.findByCottonPartAndColor(newSocks.getCottonPart(), newSocks.getColor());
        if (filteredSocksList.size() > 0) {
            Socks foundSocks = filteredSocksList.get(0);
            socksForSave = repository.getById(foundSocks.getId());
            if (foundSocks.getQuantity() > newSocks.getQuantity()) {
                socksForSave.setQuantity(foundSocks.getQuantity() - newSocks.getQuantity());
                repository.save(socksForSave);
                EntityModel<Socks> entityModel = assembler.toModel(socksForSave);

                return ResponseEntity
                        .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                        .body(entityModel);
            } else if (foundSocks.getQuantity() == newSocks.getQuantity()) {
                repository.deleteById(foundSocks.getId());
                return ResponseEntity.noContent().build();
            } else {
                throw new SocksNegativeQuantityException(foundSocks.getQuantity());
            }
        } else throw new TheseSocksNotFoundException();
    }

    // Single item


    @GetMapping("/socks/{id}")
    EntityModel<Socks> one(@PathVariable Long id) {
        Socks socks = repository.findById(id) //
                .orElseThrow(() -> new SocksNotFoundException(id));

        return assembler.toModel(socks);
    }


    @GetMapping("/api/socks")
    CollectionModel<Socks> request(@RequestBody Socks socks, @RequestParam String color, @RequestParam Operation operation, @RequestParam byte cottonPart ) {
        List<Socks> socksList=repository.findAll();
        switch(operation){
            case moreThan:
               socksList.stream()
                       .filter(filtredForCottonPart -> socks.getCottonPart()<cottonPart)
                       .filter(filtredForColor -> socks.getColor()==color)
                       .map(assembler::toModel).collect(Collectors.toList());
                break;
            case lessThan:
                socksList.stream()
                        .filter(filtredSocks ->socks.getCottonPart()>cottonPart)
                        .filter(filtredForColor -> socks.getColor()==color)
                        .map(assembler::toModel).collect(Collectors.toList());
                break;
            case equals:
                socksList.stream()
                        .filter(filtredSocks ->socks.getCottonPart()==cottonPart)
                        .filter(filtredForColor -> socks.getColor()==color)
                        .map(assembler::toModel).collect(Collectors.toList());
                break;
        }

        return CollectionModel.of(socksList, linkTo(methodOn(SocksController.class).all()).withSelfRel());
    }
}
