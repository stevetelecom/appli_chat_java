import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Icons = gestion du logo de l'application.
 *
 * Au lancement, on charge le logo (connectchat_logo_64.png / .png) présent
 * à la racine du projet pour l'afficher sur la barre des tâches et la
 * fenêtre. Si le fichier est introuvable, une copie 64x64 du logo,
 * embarquée directement dans le code, prend le relais.
 */
public final class Icons {

    private Icons() { }

    /* Copie 64x64 du logo, intégrée au code source (Base64). */
    private static final String EMBEDDED_64 =
        "iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAABmJLR0QA/wD/AP+gvaeTAAAIOUlEQVR4nOWba2wU1xXHf3d2vX7iB9gOSWzsJnFAVXHrkJJ8adI0KpQgAqUtahQUqj4iNan6+FBUqVULSFXUh1S1jRq1KU1VFOVhqEIecgkJcUpbtQoCTJM0iIQANgWbBdvrN56d0w9e787MvbPeF42x1x9m7jnn/s/53blz584uKNJ8ah5YtyQeZ72DWquEZqABpFwLFH9TAn2a0ehPOAJ9gAQ6BxFOA28rnKeHVGUH7e2Xg4KVyVh6/8aGMPZ2YAsQShViSDq74NFrVb0CW0d2d+wyZdQGYMHmdRsQtQuoMAuakk03Zxu8x/eqZcmmWPu+S26z5W5Ubr73W4jaw9yDB7jbcdTB0vWrGt2u5AxIXPk9TA/K3IJ3NeV4xIrf1t/+yiAkYEvv39iQmPZzHR5g6YQT2jltswDCMrmDuTntXc2UQQmfK/vs6vUAquaBdUvsuDrJVb/am1NPNY21vDNyaWK5FbetDcw/eIBl5TWRuyxRsmYewgOglNoURripUIJXE3wiYqUFLJ6P8IlDkwVSUTDBQH/CMbvgAaqsIH/WgoH+hGP2wQO+rfB8gwf3AMxDeNC2vlkKBvoTjlkODxAuFHyoNERpcxmhirCpSm9DdLvO7BhT2zGb8TOjOONO3vAAYW94bvBVK6qpuWMRynItKeJNLm6bu7+IAT41SGKy2UL/36MMdQ3OXOsMszBZca7wZTeWs/CuuhS84IEXfPBCenhJD4+ACilq7qiltKksL3hIrAH53POVbdW+Ke2F95C4ZYLgtVP9lpGEraK1KrjWDNefcL4LXvE1xS5f5vCaZsImnthgeIDi+hJzrVksvqk1IMfV3ioOmeF9tlR+Q5EmeN8giV9LwCpxf6WZPTxMD0CO8IbLUwB48fnN8FpQ2joxwoPhMahXHaiY55UXb1gO8BKQSy812BnWTdnA+5qmextJmQsOP1Od/kAdI6xZZhTNAj6HZ3yqqds0eElXJzPCg7hfhnKAJz18kRVmeUUTERXW+otAQ0ktDSWLjPARFWZ5ZTMRKxwMn44+A3hIzoDc4LVEviv/uw8/xK2VN3Fo8F2+9OYvPfC3Vy/l8eUPA/C1Y4/yz4Hjniv/+Ee/wcerW3hj4ARbjvzCm6pA8ABWXvDufoZpv7Ts+qlj+fXatG8pX4yFwkLRUnatNu2XVTSkjhIAP9MFMdXp6xSKfOTmbYFi0440mtW3L3Tl9g5mz8RFSq0Iv+3+C++Nnvfc8++P9lIbqeSd4R5+372fCWfSk6d7LEppKMJjpzp4d+ScDpA4xN7od+XMDh5AVXxxreR+5YXm77QkTs23gp7bpZlk8S94ooWabjURoeex93W/ASMIJGAf4M9ucovr1DAlc9zdua+wBu/u7tHKDR6M+wB/dpM7Hbzg4RKvLRUSDO99xl85eCRoI5QhvDeRt++VgBd/wEwL8wzwYNoIFQLedL8nm7pNgzcsdh74dDX6+wf6p068G6Fs4QGJCx8UvDNp/tosU3hIfiucGzyAHbv8wVx5EeJDtq9Of5CfQztJbIRyhEdg9L1RX5hLLzk5AuBlBnghEF6AsVMjumaaWnUhsPKBB4gd6mdyYDJlFK8/Lbyp+CS8aDYQ6iNVXFNSgz0wyfDRQb1/mlpNMfpPY8nYzASd8Tjnn+lh6HA/k5cuI7ZkCa+DGjdVCGI7FI1Y/LhpM5f2nseZcPKCR0BVbLpH9+T5o0WoLMR1X2nyhIjt0P/Xiwy/FdM7ZDkLv7n+8wD86rnd6TtnUKu+D8gR/qur1vLE/g7ijkNRbbHnVrCHJ7nY0cdE77gulsMt+Ojze9j7o0c40dNNY309AK8fO8rxnjMZ1eo2eQcgR/gltfVUlVcQd6YeS0W1RcnQibNjRPf1Eh+N62I5rj+OI/yn+zTPfn8HSk19MWrH4zzyzC5+8uyTGcML7n1AHtP+y5++hz/u70iKFi0qBgeGugbp23uuoPAItH7oBr6+dgNKKU5f6OVMtI9wKMQP7ttCa/ONaWtNnU41rEwSphNsua6BvsEBLg7FkqJFlWGi+87TfzCKONoqmBc8CJ9sbUMpxZloHyu2Pswt332I7ugFlFLc2doWWGvqNNUI5wJfVV7O6raVAKy8eRk7nvqTRzS6vxd70LdJKRC8W0Il/gKKNpr87/4Bb4MEwi9cUMnTW39IXWU1AJftSUIh7476SsIDdHYdRkRorK3j8M9+A0BjbR0iwmtdhwM4dHgkaB+QZtrf3XpLEh4gEi7iU61tQeEUGh7g36dOsv3JJ7DjcRpr62isrcOOx9m2aydvnjpp4DDDg/ExaOjtMjmiv4A4rl90jWIFhJ/+/Hz3U+z9x9+4s/VjgPD6saOcONutpZ46NcOD9hg0RPhMB7qO8OBn1lFfVQNA32A/r3UdCar+isBP+0+c7ebE2TNGX+o0GB5AVXxhjXdlmWERAVhQVsaqtlsBePnwIWKjI+YOVxA+MEEW8DA1AAMIVZkIaqL/52nv1c0fHiRmAd2ZCGqiVz88Av+1xFH/mo/wAEo4aYHTnk5QE50j8FMH9aI1cnH8VeB4UKe5C48tjvOCRWenrUR9z9RpDsMD7Bzr6OyxAIb2dDyH8Gdv+JyGj2Hb28G1FS61xrcAb2micw/eQanNo/sOngPXAFxo7xyO284aQd7OUtAQMnvhBfXt0RcPvDBt8LwMje19uTtk8QnglTkIH0Nk/dhLB37tjjL+52lAlW9cvRn4KbA4QNCQc1bC28AfsO1t09Pe/QkagKnPgyuKyqJ1axTOfcBtCNcC/n+emUg2a+CHBXqUcBJ4SRx5fqyjsyco9f8AMyAnwMoEXUYAAAAASUVORK5CYII=";

    /** Renvoie les images à utiliser pour les fenêtres de l'application. */
    public static List<Image> list() {
        List<Image> icons = new ArrayList<>();

        /* 1. Version haute qualité du fichier, si elle est disponible. */
        /* On essaie d'abord le PNG fourni par l'équipe, puis l'autre. */
        for (String path : new String[]{"connectchat_logo_64.png", "connectchat_logo.png"}) {
            Image logo = tryFile(path);
            if (logo != null) {
                icons.add(logo);
                break;
            }
        }

        /* 2. Version embarquée en secours (toujours présente). */
        icons.add(embed(EMBEDDED_64));
        return icons;
    }

    /** Lit une image depuis un fichier, ou null si le fichier n'existe pas. */
    private static Image tryFile(String path) {
        try (FileInputStream in = new FileInputStream(new File(path))) {
            /* Le flux d'entrée fonctionne même avant l'initialisation
               complète du moteur graphique (contrairement à l'URL). */
            return new Image(in);
        } catch (Exception ignored) {
            // fichier introuvable ou illisible : version embarquée en secours
        }
        return null;
    }

    /** Décode une image encodée en Base64. */
    private static Image embed(String base64) {
        return new Image(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));
    }
}
