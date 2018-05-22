package cz.zcu.kiv.server;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import cz.zcu.kiv.api.Calculation;

import java.awt.*;

@Path("/calculator")
public class Calculator {

	@GET
	@Path("/calc/{op}/{left}/{right}")
    @Produces(MediaType.APPLICATION_JSON)
    public Calculation calculate(@PathParam("op") String op, @PathParam("left") Integer left,
            @PathParam("right") Integer right) {
        Calculation result = new Calculation();
        result.setOperation(op);
        result.setLeft(left);
        result.setRight(right);
        return doCalc(result);
    }

	@POST
	@Path("/calc2")

    public Calculation calculate(Calculation calc) {
        return doCalc(calc);
    }

    private Calculation doCalc(Calculation c) {
        String op = c.getOperation();
        int left = c.getLeft();
        int right = c.getRight();
        if (op.equalsIgnoreCase("subtract")) {
            c.setResult(left - right);
        } else if (op.equalsIgnoreCase("multiply")) {
            c.setResult(left * right);
        } else if (op.equalsIgnoreCase("divide")) {
            c.setResult(left / right);
        } else {
            c.setResult(left + right);
        }
        return c;
    }
    
}
