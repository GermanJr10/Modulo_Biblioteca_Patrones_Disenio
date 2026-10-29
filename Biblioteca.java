import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Biblioteca {

    /*
     * COMO SE CONECTAN LOS 3 PATRONES:
     * Factory Method: BibliotecaFacade pide los Materiales a MaterialFactory para
     * registrarlos.
     * Strategy: Cada Usuario trae su regla de multa de EstrategiaMulta.
     * Facade: El main solo interactua con BibliotecaFacade
     */
    public static void main(String[] args) {
        BibliotecaFacade biblioteca = new BibliotecaFacade();
        biblioteca.registrarMaterial("libro", "Java Basico");
        biblioteca.registrarMaterial("revista", "National Geographic");
        biblioteca.registrarMaterial("tesis", "Sistemas Distribuidos");
        biblioteca.registrarUsuario("Ana", new MultaEstudiante());
        biblioteca.registrarUsuario("Carlos", new MultaDocente());
        biblioteca.registrarUsuario("Luis", new MultaExterno());

        Scanner sc = new Scanner(System.in);
        boolean salir = false;

        while (!salir) {
            System.out.println("\n--- BIBLIOTECA ---");
            System.out.println("1. Prestar");
            System.out.println("2. Devolver");
            System.out.println("3. Ver materiales");
            System.out.println("4. Salir");
            System.out.print("Opcion: ");
            String opcion = sc.nextLine().trim();

            switch (opcion) {
                case "1":
                    System.out.print("Titulo del material: ");
                    String tituloP = sc.nextLine().trim();
                    System.out.print("Nombre del usuario: ");
                    String nombreP = sc.nextLine().trim();
                    if (biblioteca.prestar(tituloP, nombreP)) {
                        System.out.println("Prestamo realizado.");
                    } else {
                        System.out.println("No se pudo prestar (material/usuario inexistente o ya prestado).");
                    }
                    break;
                case "2":
                    System.out.print("Titulo del material: ");
                    String tituloD = sc.nextLine().trim();
                    System.out.print("Dias usados: ");
                    try {
                        int dias = Integer.parseInt(sc.nextLine().trim());
                        int multa = biblioteca.devolver(tituloD, dias);
                        if (multa < 0) {
                            System.out.println("No se pudo devolver (no existe o no estaba prestado).");
                        } else {
                            System.out.println("Devuelto. Multa: " + multa + " Bs.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Numero de dias invalido.");
                    }
                    break;
                case "3":
                    biblioteca.listarMateriales();
                    break;
                case "4":
                    salir = true;
                    break;
                default:
                    System.out.println("Opcion invalida.");
            }
        }
        sc.close();
    }
}

abstract class Material {

    private String titulo;
    private boolean disponible;

    public Material(String titulo) {
        this.titulo = titulo;
        disponible = true;
    }

    public String getTitulo() {
        return titulo;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public abstract int diasPrestamoPermitidos();
}

class Libro extends Material {

    public Libro(String titulo) {
        super(titulo);
    }

    @Override
    public int diasPrestamoPermitidos() {
        return 7;
    }
}

class Revista extends Material {

    public Revista(String titulo) {
        super(titulo);
    }

    @Override
    public int diasPrestamoPermitidos() {
        return 3;
    }
}

class Tesis extends Material {

    public Tesis(String titulo) {
        super(titulo);
    }

    @Override
    public int diasPrestamoPermitidos() {
        return 2;
    }
}

/*
 * PATRON CREACIONAL --> Factory Method
 * Centraliza la creacion de materiales segun su tipo.
 * Asi el resto del programa no usa new Libro(), new Revista(), etc.
 * directamente, solo pide un material a la fabrica.
 * Si se agrega un tipo nuevo, solo se modifica la fabrica.
 */
class MaterialFactory {

    public Material crearMaterial(String tipoTexto, String titulo) {
        Material res = null;
        switch (tipoTexto.toLowerCase()) {
            case "libro":
                res = new Libro(titulo);
                break;
            case "revista":
                res = new Revista(titulo);
                break;
            case "tesis":
                res = new Tesis(titulo);
                break;
            default:
                break;
        }
        return res;
    }
}

/*
 * PATRON DE COMPORTAMIENTO --> Strategy
 * El calculo de la multa cambia segun el tipo de usuario.
 * Cada regla esta en su propia clase intercambiable, evitando if/else largos.
 * Se pueden agregar nuevas tarifas sin tocar el resto del codigo.
 */
interface EstrategiaMulta {

    int calcularMulta(int diasRetraso);
}

class MultaEstudiante implements EstrategiaMulta {

    @Override
    public int calcularMulta(int diasRetraso) {
        return 2 * diasRetraso;
    }
}

class MultaDocente implements EstrategiaMulta {

    @Override
    public int calcularMulta(int diasRetraso) {
        return diasRetraso;
    }
}

class MultaExterno implements EstrategiaMulta {

    @Override
    public int calcularMulta(int diasRetraso) {
        return 3 * diasRetraso;
    }
}

class Usuario {

    private String nombre;
    private EstrategiaMulta estrategia;

    public Usuario(String nombre, EstrategiaMulta estrategia) {
        this.nombre = nombre;
        this.estrategia = estrategia;
    }

    public String getNombre() {
        return nombre;
    }

    public int calcularMulta(int diasRetraso) {
        return estrategia.calcularMulta(diasRetraso);
    }
}

/*
 * PATRON ESTRUCTURAL --> Facade
 * Proporciona una interfaz simple (prestar/devolver) que oculta la coordinacion
 * entre busqueda, validacion, registro del prestamo y calculo de multa.
 * El main solo usa esta clase sin conocer los detalles internos.
 */
class BibliotecaFacade {

    private ArrayList<Material> listaMateriales;
    private ArrayList<Usuario> listaUsuarios;
    private MaterialFactory fabrica;
    private HashMap<Material, Usuario> prestamo;

    public BibliotecaFacade() {
        listaMateriales = new ArrayList<>();
        listaUsuarios = new ArrayList<>();
        fabrica = new MaterialFactory();
        prestamo = new HashMap<>();
    }

    public boolean registrarMaterial(String tipoTexto, String titulo) {
        Material material = fabrica.crearMaterial(tipoTexto, titulo);
        if (material == null) {
            return false;
        }
        listaMateriales.add(material);
        return true;
    }

    public void registrarUsuario(String nombre, EstrategiaMulta estrategia) {
        listaUsuarios.add(new Usuario(nombre, estrategia));
    }

    public boolean prestar(String titulo, String nombreUsuario) {
        Material material = buscarMaterial(titulo);
        Usuario usuario = buscarUsuario(nombreUsuario);
        if (material == null || usuario == null || !material.isDisponible()) {
            return false;
        }
        material.setDisponible(false);
        prestamo.put(material, usuario);
        return true;
    }

    public int devolver(String titulo, int diasUsados) {
        Material material = buscarMaterial(titulo);
        if (material == null || material.isDisponible()) {
            return -1;
        }
        Usuario usuario = prestamo.get(material);
        int retraso = Math.max(0, diasUsados - material.diasPrestamoPermitidos());
        int multa = usuario.calcularMulta(retraso);
        material.setDisponible(true);
        prestamo.remove(material);
        return multa;
    }

    public void listarMateriales() {
        for (Material m : listaMateriales) {
            String estado = m.isDisponible() ? "Disponible" : "Prestado a " + prestamo.get(m).getNombre();
            System.out.println(m.getClass().getSimpleName() + " - " + m.getTitulo()
                    + " (" + m.diasPrestamoPermitidos() + " dias) - " + estado);
        }
    }

    private Material buscarMaterial(String titulo) {
        for (Material m : listaMateriales) {
            if (m.getTitulo().equalsIgnoreCase(titulo)) {
                return m;
            }
        }
        return null;
    }

    private Usuario buscarUsuario(String nombre) {
        for (Usuario u : listaUsuarios) {
            if (u.getNombre().equalsIgnoreCase(nombre)) {
                return u;
            }
        }
        return null;
    }
}